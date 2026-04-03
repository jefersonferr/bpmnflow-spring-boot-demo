package org.bpmnflow.demo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bpmnflow.WorkflowEngine;
import org.bpmnflow.WorkflowEngine.NextStep;
import org.bpmnflow.model.ActivityNode;
import org.bpmnflow.model.WorkflowRule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Generic process navigation controller.
 *
 * <p>Injects {@code AtomicReference<WorkflowEngine>} — the same reference managed by
 * the starter's auto-configuration — so that every request always resolves to the
 * currently active engine. This means model uploads via {@code POST /bpmnflow/model}
 * are immediately reflected here without any restart or re-injection.</p>
 *
 * <p>In a production system these endpoints would be backed by a database of
 * case instances; here they are stateless to keep the demo self-contained.</p>
 */
@SuppressWarnings("unused")
@Tag(name = "Process", description = "Generic process navigation — works with any active BPMN model")
@RestController
@RequestMapping("/process")
public class ProcessController {

    private final AtomicReference<WorkflowEngine> engineRef;

    public ProcessController(AtomicReference<WorkflowEngine> engineRef) {
        this.engineRef = engineRef;
    }

    /**
     * Opens a new process instance by resolving the entry activity from the active model.
     * The initial process status is read from the StartEvent defined in the BPMN —
     * BPMNFlow allows only one StartEvent per process.
     */
    @Operation(
            summary = "Open a new process instance",
            description = "Resolves the entry activity from the active BPMN model. " +
                    "The initial process status is read directly from the StartEvent — " +
                    "no need to inform it externally."
    )
    @PostMapping("/open")
    public ResponseEntity<Map<String, Object>> openCase(
            @Parameter(description = "Unique case identifier")
            @RequestParam String caseId) {

        WorkflowEngine engine = engineRef.get();

        List<WorkflowRule> entryRules = engine.listRules().stream()
                .filter(r -> r.getType() == org.bpmnflow.model.RuleType.START_TO_TASK)
                .toList();

        if (entryRules.isEmpty()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "No START_TO_TASK rule found in the active BPMN model");
            return ResponseEntity.unprocessableEntity().body(error);
        }

        WorkflowRule entryRule     = entryRules.get(0);
        ActivityNode firstActivity = entryRule.getTarget();
        String entryStatus         = orEmpty(entryRule.getProcessStatus());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("caseId",        caseId);
        response.put("entryStatus",   entryStatus);
        response.put("firstActivity", firstActivity != null ? firstActivity.getAbbreviation() : "none");
        response.put("activityName",  firstActivity != null ? orEmpty(firstActivity.getName()) : "End Event");
        return ResponseEntity.ok(response);
    }

    /**
     * Returns all possible transitions reachable from the given activity in the active model.
     */
    @Operation(
            summary = "Get transitions from an activity",
            description = "Returns all outgoing transitions from the given activity in the active BPMN model."
    )
    @GetMapping("/transition")
    public ResponseEntity<?> transition(
            @Parameter(description = "Activity abbreviation as defined in the active BPMN model")
            @RequestParam String from) {
        try {
            WorkflowEngine engine = engineRef.get();
            ActivityNode current = engine.findActivity(from);
            List<NextStep> steps = engine.nextSteps(from);

            List<Map<String, Object>> options = steps.stream()
                    .map(step -> {
                        Map<String, Object> option = new LinkedHashMap<>();
                        option.put("ruleType",     step.ruleType());
                        option.put("conclusion",    step.conclusion() != null ? step.conclusion() : "-");
                        option.put("nextActivity",  step.targetActivity() != null
                                ? step.targetActivity().getAbbreviation() : "END");
                        option.put("nextName",      step.targetActivity() != null
                                ? orEmpty(step.targetActivity().getName()) : "End Event");
                        option.put("processStatus", step.processStatus() != null ? step.processStatus() : "-");
                        return option;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("currentActivity",      from);
            response.put("currentName",          orEmpty(current.getName()));
            response.put("availableConclusions", current.getConclusions().stream()
                    .map(c -> {
                        Map<String, Object> conclusion = new LinkedHashMap<>();
                        conclusion.put("code", orEmpty(c.getCode()));
                        conclusion.put("name", orEmpty(c.getName()));
                        return conclusion;
                    })
                    .collect(Collectors.toList()));
            response.put("transitions", options);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Returns a full process guide — every activity in the active model mapped to its exits.
     */
    @Operation(
            summary = "Get full process guide",
            description = "Returns every activity and its outgoing transitions from the active BPMN model."
    )
    @GetMapping("/guide")
    public ResponseEntity<Map<String, Object>> guide() {
        WorkflowEngine engine = engineRef.get();

        List<Map<String, Object>> activities = engine.listActivities().stream()
                .map(activity -> {
                    List<NextStep> steps = engine.nextSteps(activity.getAbbreviation());
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("abbreviation", activity.getAbbreviation());
                    entry.put("name",         orEmpty(activity.getName()));
                    entry.put("stage",        orEmpty(activity.getStageCode()));
                    entry.put("conclusions",  activity.getConclusions().stream()
                            .map(c -> orEmpty(c.getCode()) + " — " + orEmpty(c.getName()))
                            .collect(Collectors.toList()));
                    entry.put("nextSteps",    steps.stream()
                            .map(s -> s.targetActivity() != null
                                    ? s.targetActivity().getAbbreviation()
                                    : "END")
                            .distinct()
                            .collect(Collectors.toList()));
                    return entry;
                })
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("workflow",   orEmpty(engine.getWorkflow().getName()));
        response.put("type",       orEmpty(engine.getWorkflow().getType()) + "/" + orEmpty(engine.getWorkflow().getSubtype()));
        response.put("valid",      engine.isValid());
        response.put("activities", activities);
        return ResponseEntity.ok(response);
    }

    private static String orEmpty(String value) {
        return value != null ? value : "";
    }
}