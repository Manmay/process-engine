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

import za.co.trikwetra.schema.TaskQueryMultiple;


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
		
		Map<String, Object> processVariables = new HashMap<String, Object>();
		if(taskId == null){
			System.out.println("Insert Process Data : " +  formData);
			String reference = "#"+System.currentTimeMillis();
			System.out.println(reference);
			Object startData = mapper.map(formData,  Class.forName("processes." + processName + ".Start"));
			Object processData = mapper.map(startData, Class.forName("processes." + processName + ".Data"));
			processVariables.put("ref", reference);
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
	public List<TaskQueryMultiple> getTasks(@PathVariable("userId") String userId){
		List<TaskQueryMultiple> response = new ArrayList<TaskQueryMultiple>(); 
		List<Task> tasks = taskService.createTaskQuery().taskCandidateOrAssigned(userId).list();
		System.out.println(tasks.size());
		for(Task task : tasks){
			String key = runtimeService.getVariables(task.getProcessInstanceId()).get("ref").toString();
			TaskQueryMultiple aTask = new TaskQueryMultiple();
			aTask.setKey(key);
			aTask.setId(task.getId());
			aTask.setName(task.getName());
			aTask.setDescription(task.getDescription());
			aTask.setProcess(task.getProcessDefinitionId());
			aTask.setForm(task.getFormKey() + "/" + aTask.getId());
		    aTask.setAssignee(task.getAssignee());
			response.add(aTask);
		}
		return response;		
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
	
	
	@RequestMapping(path = "processes/reference/{reference}", method=RequestMethod.GET)
	public void findProcess(@PathVariable("reference") String reference){
		ProcessInstance pi = runtimeService.createProcessInstanceQuery().includeProcessVariables().variableValueEquals("ref", reference).singleResult();
		System.out.println(pi.getId());
		System.out.println(pi.getName());
		System.out.println(pi.getProcessDefinitionKey());
	}

}
