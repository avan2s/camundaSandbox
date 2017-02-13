package org.camunda.training;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.init;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.processEngine;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
public class InMemoryH2Test {

    @ClassRule
    @Rule
    public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

    private static final String PROCESS_DEFINITION_KEY = "simple-demo";

    static {
        LogFactory.useSlf4jLogging(); // MyBatis
    }

    @Before
    public void setup() {
        init(rule.getProcessEngine());
    }

    /**
     * Just tests if the process definition is deployable.
     */
    @Test
    @Deployment(resources = "process.bpmn")
    public void testParsingAndDeployment() {
        // nothing is done here, as we just want to check for exceptions during deployment
    }

    @Test
    @Deployment(resources = "process.bpmn")
    public void testHappyPath() {
        VariableMap instanceVariables = Variables.createVariables().putValue("v1", "value of v1");
        ProcessInstance processInstance = processEngine().getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY, instanceVariables);
        List<HistoricTaskInstance> historicTaskInstances = processEngine().getHistoryService().createHistoricTaskInstanceQuery().finished().processInstanceId(processInstance.getId()).list();

        List<Task> taskList = this.seeVariables(processInstance);
        String userTaskAid = taskList.get(0).getId();
        processEngine().getTaskService().claim(userTaskAid, "Hans");
        Task taskAssignedToHans = processEngine().getTaskService().createTaskQuery().processInstanceId(processInstance.getId()).taskAssignee("Hans").singleResult();
        processEngine().getTaskService().complete(userTaskAid);
        System.out.println("\n\n\n\n\n");
        this.seeVariables(processInstance);
        System.out.println("finished");

    }

    private List<Task> seeVariables(ProcessInstance processInstance) {
        List<Task> taskList = processEngine().getTaskService().createTaskQuery().processInstanceId(processInstance.getId()).list();
        Map<String, Object> variablesInInstance = processEngine().getRuntimeService().getVariables(processInstance.getId());
        System.out.println("Instance " + processInstance.getProcessInstanceId() + " is seeing the following variables:" + variablesInInstance);
        Map<String, Object> variablesInInstanceLocal = processEngine().getRuntimeService().getVariablesLocal(processInstance.getId());
        System.out.println("Instance " + processInstance.getProcessInstanceId() + " is seeing the following local variables:" + variablesInInstanceLocal);

        for (Task task : taskList) {

            Map<String, Object> variablesLocalInTask = processEngine().getRuntimeService().getVariablesLocal(task.getExecutionId());
            System.out.println("Task " + task.getName() + " is seeing the following local variables:" + variablesLocalInTask);

            Map<String, Object> allVariablesInTask = processEngine().getRuntimeService().getVariables(task.getExecutionId());
            System.out.println("Task " + task.getName() + " is seeing the following variables:" + allVariablesInTask);
            int x = 0;
        }
        return taskList;
    }

}
