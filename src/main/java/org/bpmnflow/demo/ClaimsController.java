package org.bpmnflow.demo;

import org.bpmnflow.WorkflowEngine;
import org.bpmnflow.WorkflowEngine.NextStep;
import org.bpmnflow.model.ActivityNode;
import org.bpmnflow.model.WorkflowRule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Domain controller that uses {@link WorkflowEngine} to drive an insurance
 * claims (RDCT - Outros Danos) process.
 *
 * <p>This shows the real value of the starter: the application doesn't hard-code
 * workflow transitions — it queries the BPMN model at runtime to discover what
 * comes next, what status to set, and which conclusions are possible.
 *
 * <p>In a production system these endpoints would be backed by a database of
 * case instances; here they are stateless to keep the demo self-contained.
 */
@RestController
@RequestMapping("/claims")
public class ClaimsController {

    private final WorkflowEngine engine;

    public ClaimsController(WorkflowEngine engine) {
        this.engine = engine;
    }

    /**
     * Simulates the start of a new claim case.
     *
     * <p>Queries the BPMN for rules triggered by status "NV" (Novo) — these are
     * the START_TO_TASK rules that define which activity handles new cases.
     *
     * <p>Example response:
     * <pre>
     * {
     *   "caseId": "CASE-001",
     *   "status": "NV",
     *   "firstActivity": "TR-TR1",
     *   "activityName": "Triagem inicial"
     * }
     * </pre>
     */
    @PostMapping("/open")
    public ResponseEntity<Map<String, Object>> openCase(@RequestParam String caseId) {
        List<WorkflowRule> entryRules = engine.rulesTriggeredBy("NV");

        if (entryRules.isEmpty()) {
            return ResponseEntity.unprocessableEntity().body(Map.of(
                    "error", "No entry rule found for status NV in the current BPMN model"
            ));
        }

        WorkflowRule firstRule = entryRules.get(0);
        ActivityNode firstActivity = firstRule.getTarget();

        return ResponseEntity.ok(Map.of(
                "caseId",        caseId,
                "status",        "NV",
                "firstActivity", firstActivity != null ? firstActivity.getAbbreviation() : "none",
                "activityName",  firstActivity != null ? firstActivity.getName() : "End Event"
        ));
    }

    /**
     * Given the current activity of a case, returns all possible next steps.
     *
     * <p>This lets the application know — purely from the BPMN model — which
     * activities can follow, what conclusion labels to present to the user,
     * and what process status each path would produce.
     *
     * <p>Example: {@code GET /claims/transition?from=TR-TR1}
     */
    @GetMapping("/transition")
    public ResponseEntity<?> transition(@RequestParam String from) {
        try {
            ActivityNode current = engine.findActivity(from);
            List<NextStep> steps = engine.nextSteps(from);

            List<Map<String, Object>> options = steps.stream()
                    .map(step -> {
                        Map<String, Object> option = new java.util.LinkedHashMap<>();
                        option.put("ruleType",      step.ruleType());
                        option.put("conclusion",     step.conclusion() != null ? step.conclusion() : "-");
                        option.put("nextActivity",   step.targetActivity() != null
                                ? step.targetActivity().getAbbreviation() : "END");
                        option.put("nextName",       step.targetActivity() != null
                                ? step.targetActivity().getName() : "End Event");
                        option.put("processStatus",  step.processStatus() != null ? step.processStatus() : "-");
                        return option;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "currentActivity", from,
                    "currentName",     current.getName(),
                    "availableConclusions", current.getConclusions().stream()
                            .map(c -> Map.of("code", c.getCode(), "name", c.getName()))
                            .collect(Collectors.toList()),
                    "transitions", options
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Returns a full "process guide" — a map of every activity and its possible exits.
     *
     * <p>This is useful for generating documentation, populating UI flows,
     * or feeding a decision engine without any hardcoded logic.
     */
    @GetMapping("/guide")
    public ResponseEntity<Map<String, Object>> processGuide() {
        List<Map<String, Object>> guide = engine.listActivities().stream()
                .map(activity -> {
                    List<NextStep> steps = engine.nextSteps(activity.getAbbreviation());
                    Map<String, Object> entry = new java.util.LinkedHashMap<>();
                    entry.put("abbreviation", activity.getAbbreviation());
                    entry.put("name",         activity.getName());
                    entry.put("stage",        activity.getStageCode());
                    entry.put("conclusions",  activity.getConclusions().stream()
                            .map(c -> c.getCode() + " — " + c.getName())
                            .collect(Collectors.toList()));
                    entry.put("nextSteps", steps.stream()
                            .map(s -> s.targetActivity() != null
                                    ? s.targetActivity().getAbbreviation()
                                    : "END")
                            .distinct()
                            .collect(Collectors.toList()));
                    return entry;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "workflow",    engine.getWorkflow().getName(),
                "type",        engine.getWorkflow().getType() + "/" + engine.getWorkflow().getSubtype(),
                "valid",       engine.isValid(),
                "activities",  guide
        ));
    }
}
