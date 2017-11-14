/*******************************************************************************
 * Copyright 2017 Samsung Electronics All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/

package com.sec.processing.framework.controller;

import com.sec.processing.framework.data.model.error.ErrorFormat;
import com.sec.processing.framework.data.model.error.ErrorType;
import com.sec.processing.framework.data.model.response.ResponseFormat;
import com.sec.processing.framework.data.model.response.TaskResponseFormat;
import com.sec.processing.framework.data.model.task.TaskFormat;
import org.edgexfoundry.processing.runtime.task.TaskManager;
import org.edgexfoundry.processing.runtime.task.TaskType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

@RestController
@Api(tags = "Task Manager", description = "API List for Task Managing")
@RequestMapping(value = "/v1/task")
public class TaskController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskController.class);
    private TaskManager taskManager;

    public TaskController() {
        try {
            taskManager = TaskManager.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ApiOperation(value = "Find Supporting Task", notes = "Find Supporting Task")
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public TaskResponseFormat getTaskes(Locale locale, Model model) {
        TaskResponseFormat response =
                new TaskResponseFormat(taskManager.getTaskModelList());

        LOGGER.debug(response.toString());
        return response;
    }

    @ApiOperation(value = "Add New Custom Task", notes = "Add New Custom Task")
    @RequestMapping(value = "", method = RequestMethod.POST, headers = ("content-type=multipart/*"))
    @ResponseBody
    public ResponseFormat addTask(Locale locale, Model model,
                                     @RequestParam("file") MultipartFile inputFile) {
        ResponseFormat response = new ResponseFormat();
        ErrorFormat result = new ErrorFormat();

        byte[] data = null;

        try {
            data = inputFile.getBytes();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            result.setErrorMessage(e.getMessage());
            result.setErrorCode(ErrorType.DPFW_ERROR_INVALID_PARAMS);
            response.setError(result);
            return response;
        }

        result = taskManager.addTask(inputFile.getOriginalFilename(), data);
        response.setError(result);
        LOGGER.debug(response.toString());
        return response;
    }

    @ApiOperation(value = "Delete Custom Task", notes = "Delete Custom Task")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public ResponseFormat deleteTask(Locale locale, Model model,
                                     @RequestParam("type") TaskType type,
                                     @RequestParam("name") String name) {
        LOGGER.debug("Data : " + name + ", " + type);

        ResponseFormat response = new ResponseFormat();
        ErrorFormat result = taskManager.deleteTask(type, name);
        response.setError(result);

        LOGGER.debug(response.toString());
        return response;
    }

    @ApiOperation(value = "Find Supporting Task by Name", notes = "Find Supporting Task by Name")
    @RequestMapping(value = "name/{name}", method = RequestMethod.GET)
    @ResponseBody
    public TaskResponseFormat getProcessById(Locale locale, Model model, @PathVariable("name") String name) {
        LOGGER.debug("Data : " + name);

        TaskResponseFormat response =
                new TaskResponseFormat((ArrayList<TaskFormat>)
                        taskManager.getTaskModel(name));

        LOGGER.debug(response.toString());
        return response;
    }

    @ApiOperation(value = "Find Supporting Task by Type", notes = "Find Supporting Task by Type")
    @RequestMapping(value = "type/{type}", method = RequestMethod.GET)
    @ResponseBody
    public TaskResponseFormat getProcessByType(Locale locale, Model model, @PathVariable("type") TaskType type) {
        LOGGER.debug("Data : " + type);

        TaskResponseFormat response =
                new TaskResponseFormat((ArrayList<TaskFormat>)
                        taskManager.getTaskModel(type));

        LOGGER.debug(response.toString());
        return response;
    }
}