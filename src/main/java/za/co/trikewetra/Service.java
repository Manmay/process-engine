package za.co.trikewetra;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.FormService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
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

import za.co.trikwetra.identity.GroupCommandCreate;
import za.co.trikwetra.identity.GroupQueryMultiple;
import za.co.trikwetra.identity.GroupQuerySingle;
import za.co.trikwetra.identity.UserCommandCreate;
import za.co.trikwetra.identity.UserCommandUpdate;
import za.co.trikwetra.identity.UserQueryMultiple;
import za.co.trikwetra.identity.UserQuerySingle;


@RestController
public class Service {
	
	@Autowired
	private RuntimeService runtimeService;
	
	@Autowired
	TaskService taskService;
	
	@Autowired
	FormService formService;
	
	@Autowired
	IdentityService identityService;
	
	@Autowired
	ModelMapper mapper;
	
	ProcessInstance pi;
	
	
	@RequestMapping(path="api/users", method=RequestMethod.GET, produces="application/json")
	public List<UserQueryMultiple> findUsers(){
		List<UserQueryMultiple> response = new ArrayList<UserQueryMultiple>();
		List<User> users = identityService.createUserQuery().list();
		for(User user : users){
			response.add(mapper.map(user, UserQueryMultiple.class));
		}
		return response;
	}
	
	@RequestMapping(path="api/users/{userId}", method=RequestMethod.GET, produces="application/json")
	public UserQuerySingle findUser(@PathVariable("userId") String userId) {
		User user = identityService.createUserQuery().userId(userId).singleResult();
		if(user == null){
			throw new IllegalArgumentException("User Not Found");
		}
		UserQuerySingle response  = mapper.map(user, UserQuerySingle.class);
		
		List<Group> groups = identityService.createGroupQuery().groupMember(userId).list();
		for(Group group : groups){
			response.getGroups().add(mapper.map(group, UserQuerySingle.Groups.class ));
		}
		return response;
	}
	
	@RequestMapping(path="api/users", method=RequestMethod.POST, consumes="application/json")
	public void createUser(@RequestBody UserCommandCreate request){
		User user = identityService.newUser(request.getId());
		user.setFirstName(request.getFirstName());
		user.setLastName(request.getLastName());
		user.setEmail(request.getEmail());
		user.setPassword(request.getPassword());
		identityService.saveUser(user);
	}
	
	@RequestMapping(path="api/users/{userId}", method=RequestMethod.PUT, consumes="application/json")
	public void updateUser(@PathVariable("userId") String userId, @RequestBody UserCommandUpdate request){
		User user = identityService.createUserQuery().userId(userId).singleResult();
		if(user == null){
			throw new IllegalArgumentException("User Not Found");
		}
		user.setFirstName(request.getFirstName());
		user.setLastName(request.getLastName());
		user.setEmail(request.getEmail());
		user.setPassword(request.getPassword());
		identityService.saveUser(user);
	}
	
	@RequestMapping(path="api/users/{userId}/memebership/create", method=RequestMethod.PATCH)
	public void createMemebership(@PathVariable("userId")  String userId,  @RequestParam("groupId") String[] groupIds){
		for(String groupId : groupIds){
			identityService.createMembership(userId, groupId);
		}
	}
	
	@RequestMapping(path="api/users/{userId}/memebership/delete", method=RequestMethod.PATCH)
	public void deleteMemebership(@PathVariable("userId")  String userId,  @RequestParam("groupId") String[] groupIds){
		for(String groupId : groupIds){
			identityService.deleteMembership(userId, groupId);
		}
	}
	
	
	@RequestMapping(path="api/groups", method=RequestMethod.POST, consumes="application/json")
	public void createGroup(@RequestBody GroupCommandCreate request){
		Group group = identityService.newGroup(request.getId());
		group.setName(request.getName());
		group.setType(request.getType());
		identityService.saveGroup(group);
	}
	
	@RequestMapping(path="api/groups", method=RequestMethod.GET, produces="application/json")
	public List<GroupQueryMultiple> findGroups(){
		List<GroupQueryMultiple> response = new ArrayList<GroupQueryMultiple>();
		List<Group> groups = identityService.createGroupQuery().list();
		for(Group group: groups){
			response.add(mapper.map(group, GroupQueryMultiple.class));
		}
		return response;
	}
	
	@RequestMapping(path="api/groups/{groupId}", method=RequestMethod.GET, produces="application/json")
	public GroupQuerySingle findGroup(@PathVariable("groupId") String groupId){
		Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
		if(group == null){
			throw new IllegalArgumentException("No Group Found");
		}
		GroupQuerySingle response = mapper.map(group, GroupQuerySingle.class);
		List<User> users = identityService.createUserQuery().memberOfGroup(groupId).list();
		for(User user : users){
			response.getUsers().add(mapper.map(user, GroupQuerySingle.Users.class));
		}
		return response;
	}
	
}
