package za.co.trikewetra.process.generic;

import org.activiti.engine.FormService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FormController {
	
	@Autowired
	FormService formService;
	
	@Autowired
	RepositoryService repositoryService;
	
	@Autowired
	TaskService taskService;

	
	@RequestMapping(path="processes/{processName}/form", method=RequestMethod.GET, produces="text/html")
	public String getProcessForm(@PathVariable("processName") String processName, @RequestParam(value="taskId", required=false) String taskId) {
		String formKey =  null;
		ProcessDefinition process = repositoryService.createProcessDefinitionQuery().processDefinitionName(processName).singleResult();
		if(taskId == null){
			formKey = formService.getStartFormKey(process.getId());
		} else {
			Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
			formKey = formService.getTaskFormKey(process.getId(), task.getTaskDefinitionKey());
		}
		System.out.println(formKey);	
		return formKey;
	}


}
