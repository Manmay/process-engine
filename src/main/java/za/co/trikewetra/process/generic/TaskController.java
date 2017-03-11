package za.co.trikewetra.process.generic;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
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
	
	
	@RequestMapping(path="processes/{process}", method=RequestMethod.POST, consumes="application/json")
	public void startProcess(@PathVariable("process") String process, @RequestBody Object request) throws ClassNotFoundException{
		Map<String, Object> processVariables = new HashMap<String, Object>();
		Object startData = mapper.map(request,  Class.forName("processes." + process + ".Start"));
		Object processData = mapper.map(startData, Class.forName("processes." + process + ".Data"));
		processVariables.put("reference", "#"+System.currentTimeMillis());
		processVariables.put("data", processData);
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(process, processVariables);
		System.out.println(processInstance.getId() + " : Process Started");
	}
	
	
	@RequestMapping(path="processes/{processId}", method=RequestMethod.GET)
	public Object getProcessData(@PathVariable("processId") String processId){
		return runtimeService.getVariables(processId);
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
		Task task = taskService.createTaskQuery().taskId(taskId).includeProcessVariables().includeTaskLocalVariables().singleResult();
		Object processData = new HashMap<String, Object>();
		if(task.getTaskLocalVariables().get("data") != null){
			processData = task.getTaskLocalVariables().get("data");
			System.out.println("Sending Local Variables");
		}else {
			processData = task.getProcessVariables().get("data");
			System.out.println("Sending Global Variables");
		}
	    System.out.println(processData);
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
	public void completeTask(@PathVariable("taskId") String taskId, @RequestBody Object request, @RequestParam("userId") String userId) throws ClassNotFoundException{
		Task task = taskService.createTaskQuery().taskId(taskId).includeProcessVariables().includeTaskLocalVariables().singleResult();
		if(task.getAssignee().equals(userId)){
			Map<String, Object> processVariables = task.getProcessVariables();
			Object processData = processVariables.get("data");
			Object requestData = mapper.map(request, Class.forName("processes." + task.getProcessDefinitionId().split(":")[0] + "." + task.getName()));
			System.out.println(processData.getClass().getName());
			System.out.println(requestData.getClass().getName());
		    mapper.map(requestData, processData);
			processVariables.put("data", processData);
			taskService.complete(taskId, processVariables);
		} else {
			throw new RuntimeException("Not Allowed");
		}
	}
	
	
	@RequestMapping(path="tasks/{taskId}", method=RequestMethod.PUT, consumes="application/json")
	public void saveTask(@PathVariable("taskId") String taskId, @RequestBody Object request, @RequestParam("userId") String userId) throws ClassNotFoundException{
		Task task = taskService.createTaskQuery().taskId(taskId).includeProcessVariables().includeTaskLocalVariables().singleResult();
		if(task.getAssignee().equals(userId)){
			Object processData = task.getProcessVariables().get("data");
			Object requestData = mapper.map(request, Class.forName("processes." + task.getProcessDefinitionId().split(":")[0] + "." + task.getName()));
		    mapper.map(requestData, processData);
			taskService.setVariableLocal(taskId, "data", processData);
		} else {
			throw new RuntimeException("Not Allowed");
		}
	}

}
