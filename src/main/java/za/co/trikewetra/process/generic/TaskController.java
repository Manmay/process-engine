package za.co.trikewetra.process.generic;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
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

	@RequestMapping(path="processes/{processName}/list", method=RequestMethod.GET, produces="application/json")
	public List<String> listTasks(){
		String user = "kermit";
		System.out.println(user);
		List<Task> tasks = taskService.createTaskQuery().taskAssignee(user).list();
		List<String> taskList = new ArrayList<String>();
		for(Task task : tasks){
			taskList.add(task.getId());
			System.out.println(task.getProcessDefinitionId());
		}
		return taskList;
	}

}
