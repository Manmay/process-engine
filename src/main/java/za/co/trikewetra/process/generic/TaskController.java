package za.co.trikewetra.process.generic;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api")
public class TaskController {
	
	@Autowired
	FormService formService;
	
	@Autowired
	RepositoryService repositoryService;
	
	@Autowired
	TaskService taskService;
	
	@Autowired
	RuntimeService runtimeService;
	
	@Autowired
	HistoryService historyService;
		
	@Autowired
	ModelMapper mapper;
	

	@RequestMapping(path="processes/{processName}/data", method=RequestMethod.GET, produces="application/json")
	public Object fetchTask(@PathVariable("processName") String processName, @RequestParam(value="taskId", required=false) String taskId) throws IOException, ClassNotFoundException {
		Object formData =  null;
		if(taskId == null){
			formData = new HashMap<String, Object>();
		} else {
			Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
			Object processData = runtimeService.getVariables(task.getProcessInstanceId()).get("data");
			System.out.println("processes." + processName + "." + task.getName());
			formData = mapper.map(processData, Class.forName("processes." + processName + "." + task.getName()));					
		}
		return formData;
	}
	
	

	@RequestMapping(path="processes/{processName}/data", method=RequestMethod.POST, consumes="application/json")
	public void completeTask(@PathVariable("processName") String processName, @RequestBody Object formData, @RequestParam(value="taskId", required=false) String taskId) throws ClassNotFoundException{
		String reference = System.currentTimeMillis();
		Map<String, Object> processVariables = new HashMap<String, Object>();
		if(taskId == null){
			System.out.println("Insert Process Data : " +  formData);
			Object startData = mapper.map(formData,  Class.forName("processes." + processName + ".Start"));
			Object processData = mapper.map(startData, Class.forName("processes." + processName + ".Data"));
			processVariables.put("data", processData);
			runtimeService.startProcessInstanceByKey(processName, processVariables);
		} else {
			System.out.println("Update Process Data");
			Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
			Object processData =  runtimeService.getVariables(task.getProcessInstanceId()).get("data");
			Object stepData = mapper.map(formData,  Class.forName("processes." + processName + "." + task.getName()));
		    mapper.map(stepData, processData);
			processVariables.put("data", processData);
			taskService.complete(taskId, processVariables);
		}
	}

	@RequestMapping(path="users/{userId}/tasks", method=RequestMethod.GET, produces="application/json")
	public void listTasks(@PathVariable("userId") String userId){
		System.out.println(userId);
		List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).list();
		for(Task task : tasks){
			System.out.println("----------------------------");
			System.out.println(task.getId());;
			System.out.println(task.getName());;
			System.out.println(task.getProcessDefinitionId());;
			System.out.println(task.getFormKey());
			System.out.println(task.getDescription());
			System.out.println(task.getProcessInstanceId());
			System.out.println(task.getTaskDefinitionKey());
			System.out.println(task.getExecutionId());
		}
		//return tasks;		
	}
	
	@RequestMapping(path = "processes/{processId}/currentTask", method=RequestMethod.GET)
	public String getCurrentTask(@PathVariable("processId") String processId){
		 List<HistoricActivityInstance> hts = historyService.createHistoricActivityInstanceQuery().processInstanceId(processId).list();
		 for(HistoricActivityInstance ht : hts){
			 System.out.println(ht.getProcessDefinitionId());
			 System.out.println(ht.getActivityName());
			 System.out.println(ht.getAssignee());
		 }
		return "";
	}
	
	
	public void findProcess(String reference){
		ProcessInstance pi = runtimeService.createProcessInstanceQuery().includeProcessVariables().variableValueEquals("reference", reference).singleResult();
		System.out.println(pi.getId());
		System.out.println(pi.getName());
	}

}
