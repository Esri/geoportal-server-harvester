package com.esri.geoportal.harvester.rest;

import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.engine.defaults.DefaultProcessesService;
import com.esri.geoportal.harvester.engine.services.Engine;
import com.esri.geoportal.harvester.engine.services.ProcessesService;
import com.esri.geoportal.harvester.support.ProcessResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class TaskControllerTest {
  private final UUID taskId = UUID.randomUUID();
  private final List<Map.Entry<UUID, ProcessInstance>> processes = new ArrayList<>();

  @Test
  public void returnsOnlyWorkingProcessesForRequestedTask() throws Exception {
    UUID first = addProcess(taskId.toString(), ProcessInstance.Status.working);
    UUID second = addProcess(taskId.toString(), ProcessInstance.Status.working);
    addProcess(taskId.toString(), ProcessInstance.Status.submitted);
    addProcess(taskId.toString(), ProcessInstance.Status.aborting);
    addProcess(taskId.toString(), ProcessInstance.Status.completed);
    addProcess(UUID.randomUUID().toString(), ProcessInstance.Status.working);
    addProcess(null, ProcessInstance.Status.working);

    ResponseEntity<ProcessResponse[]> response = controller(false).getTaskProcesses(taskId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().length);
    assertEquals(first, response.getBody()[0].getUuid());
    assertEquals(second, response.getBody()[1].getUuid());
    for (ProcessResponse process : response.getBody()) {
      assertEquals(ProcessInstance.Status.working, process.getStatus());
      assertEquals(taskId.toString(), process.getTaskDefinition().getRef());
    }
  }

  @Test
  public void returnsEmptyArrayWhenNoProcessesMatch() throws Exception {
    addProcess(taskId.toString(), ProcessInstance.Status.completed);
    ResponseEntity<ProcessResponse[]> response = controller(false).getTaskProcesses(taskId);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().length);
  }

  @Test
  public void returnsServerErrorWhenProcessSelectionFails() throws Exception {
    ResponseEntity<ProcessResponse[]> response = controller(true).getTaskProcesses(taskId);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  public void mapsGetRequestToTaskProcesses() throws Exception {
    RequestMapping mapping = TaskController.class.getMethod("getTaskProcesses", UUID.class)
        .getAnnotation(RequestMapping.class);
    assertArrayEquals(new String[]{"/rest/harvester/tasks/{taskId}/processes"}, mapping.value());
    assertArrayEquals(new RequestMethod[]{RequestMethod.GET}, mapping.method());
    assertArrayEquals(new String[]{"application/json"}, mapping.produces());
  }

  private UUID addProcess(String taskRef, ProcessInstance.Status status) {
    UUID processId = UUID.randomUUID();
    Task task = new Task("test", taskRef, null, null, null);
    ProcessInstance process = ProcessInstance.class.cast(Proxy.newProxyInstance(
        ProcessInstance.class.getClassLoader(), new Class<?>[]{ProcessInstance.class},
        (proxy, method, args) -> {
          switch (method.getName()) {
            case "getTask": return task;
            case "getStatus": return status;
            default: throw new UnsupportedOperationException(method.getName());
          }
        }));
    processes.add(Map.entry(processId, process));
    return processId;
  }

  private TaskController controller(boolean fail) throws Exception {
    ProcessesService service = new DefaultProcessesService(null, null, null) {
      @Override
      public List<Map.Entry<UUID, ProcessInstance>> selectProcesses(
          Predicate<? super Map.Entry<UUID, ProcessInstance>> predicate) throws DataProcessorException {
        if (fail) {
          throw new DataProcessorException("Process selection failed");
        }
        return processes.stream().filter(predicate).collect(Collectors.toList());
      }
    };
    Engine engine = Engine.class.cast(Proxy.newProxyInstance(
        Engine.class.getClassLoader(), new Class<?>[]{Engine.class},
        (proxy, method, args) -> {
          if (method.getName().equals("getProcessesService")) {
            return service;
          }
          throw new UnsupportedOperationException(method.getName());
        }));
    TaskController controller = new TaskController();
    Field field = TaskController.class.getDeclaredField("engine");
    field.setAccessible(true);
    field.set(controller, engine);
    return controller;
  }
}
