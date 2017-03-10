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
import za.co.trikwetra.schema.TaskQuerySingle;


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
	

//	@RequestMapping(path="processes/{processName}/data", method=RequestMethod.GET, produces="application/json")
//	public Object fetchTask(@PathVariable("processName") String processName, @RequestParam(value="taskId", required=false) String taskId) throws IOException, ClassNotFoundException {
//		Object formData =  null;
//		if(taskId == null){
//			formData = new HashMap<String, Object>();
//		} else {
//			Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
//			Object processData = runtimeService.getVariables(task.getProcessInstanceId()).get("data");
//			System.out.println("processes." + processName + "." + task.getName());
//			formData = mapper.map(processData, Class.forName("processes." + processName + "." + task.getName()));					
//		}
//		return formData;
//	}
//	
	

	@RequestMapping(path="processes/{processName}/data", method=RequestMethod.POST, consumes="application/json")
	public void completeTask(@PathVariable("processName") String processName, @RequestBody Object formData, @RequestParam(value="taskId", required=false) String taskId) throws ClassNotFoundException{
		
		Map<String, Object> processVariables = new HashMap<String, Object>();
		if(taskId == null){
			System.out.println("Insert Process Data : " +  formData);
			String reference = "#"+System.currentTimeMillis();
			System.out.println(reference);
			Object startData = mapper.map(formData,  Class.forName("processes." + processName + ".Start"));
			Object processData = mapper.map(startData, Class.forName("processes." + processName + ".Data"));
			processVariables.put("reference", reference);
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
	
	

	@RequestMapping(path="tasks", method=RequestMethod.GET, produces="application/json")
	public List<TaskQueryMultiple> getTasks(@RequestParam("userId") String userId){
		List<TaskQueryMultiple> response = new ArrayList<TaskQueryMultiple>(); 
		List<Task> tasks = taskService.createTaskQuery().taskCandidateOrAssigned(userId).list();
		System.out.println(tasks.size());
		for(Task task : tasks){
			TaskQueryMultiple aTask = new TaskQueryMultiple();
			aTask.setId(task.getId());
			aTask.setName(task.getName());
			aTask.setDescription(task.getDescription().split("\\|")[1]);
			aTask.setProcess(task.getProcessDefinitionId().split(":")[0]);
			aTask.setReference(task.getDescription().split("\\|")[0]);
			aTask.setForm(task.getFormKey() + "/" + aTask.getId());
		    aTask.setAssignee(task.getAssignee());
			response.add(aTask);
		}
		return response;		
	}
	
	@RequestMapping(path="tasks/{taskId}", method=RequestMethod.GET, produces="application/json")
	public TaskQuerySingle getTask(@PathVariable("taskId") String taskId, @RequestParam("userId") String userId) throws ClassNotFoundException{
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		Object processData = runtimeService.getVariables(task.getProcessInstanceId()).get("data");
		Object formData = mapper.map(processData, Class.forName("processes." + task.getProcessDefinitionId().split(":")[0] + "." + task.getName()));
		TaskQuerySingle response = new TaskQuerySingle();
		response.setId(task.getId());
		response.setName(task.getName());
		response.setDescription(task.getDescription().split("\\|")[1]);
		response.setProcess(task.getProcessDefinitionId().split(":")[0]);	
		response.setReference(task.getDescription().split("\\|")[0]);
		response.setAssignee(task.getAssignee());
		response.setData(formData);
		return response;
	}
	
	@RequestMapping(path="tasks/{taskId}" , method=RequestMethod.PATCH)
	public void claimTask(@PathVariable("taskId") String taskId, @RequestParam("userId") String userId){
		this.taskService.claim(taskId, userId);
	}
	
	
	@RequestMapping(path="tasks/{taskId}", method=RequestMethod.POST, consumes="application/json")
	public void completeTask(@PathVariable("taskId") String taskId, @RequestBody String request, @RequestParam("userId") String userId) throws ClassNotFoundException{
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if(task.getAssignee().equals(userId)){
			Map<String, Object> processVariables = new HashMap<String, Object>();
			Object processData =  runtimeService.getVariables(task.getProcessInstanceId()).get("data");
			Object stepData = mapper.map(request,  Class.forName("processes." + task.getProcessDefinitionId().split(":")[0] + "." + task.getName()));
		    mapper.map(stepData, processData);
			processVariables.put("data", processData);
			taskService.complete(taskId, processVariables);
		} else {
			throw new RuntimeException("Not Allowed");
		}
	}
	
	
	
//	@RequestMapping(path = "processes/{processId}/currentTask", method=RequestMethod.GET)
//	public String getCurrentTask(@PathVariable("processId") String processId){
//		 List<HistoricActivityInstance> hts = historyService.createHistoricActivityInstanceQuery().processInstanceId(processId).list();
//		 for(HistoricActivityInstance ht : hts){
//			 System.out.println(ht.getProcessDefinitionId());
//			 System.out.println(ht.getActivityName());
//			 System.out.println(ht.getAssignee());
//		 }
//		return "";
//	}
//	
//	
//	@RequestMapping(path = "processes/reference/{reference}", method=RequestMethod.GET)
//	public void findProcess(@PathVariable("reference") String reference){
//		ProcessInstance pi = runtimeService.createProcessInstanceQuery().includeProcessVariables().variableValueEquals("ref", reference).singleResult();
//		System.out.println(pi.getId());
//		System.out.println(pi.getName());
//		System.out.println(pi.getProcessDefinitionKey());
//	}

}
