/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portlet.survey.mvc;

import java.util.List;

import org.jasig.portlet.survey.mvc.service.ISurveyDataService;
import org.jasig.portlet.survey.service.dto.QuestionDTO;
import org.jasig.portlet.survey.service.dto.SurveyDTO;
import org.jasig.portlet.survey.service.dto.SurveyQuestionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = SurveyRestController.REQUEST_ROOT, produces = MediaType.APPLICATION_JSON_VALUE)
public class SurveyRestController {
    static final String REQUEST_ROOT = "/v1/surveys";
    @Autowired
    private ISurveyDataService dataService;
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Create a new question that is not associated with a survey.
     * 
     * @param question
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/questions")
    public ResponseEntity<QuestionDTO> addQuestion(@RequestBody QuestionDTO question) {
        QuestionDTO newQuestion = dataService.createQuestion(question);
        return new ResponseEntity<>(newQuestion, HttpStatus.CREATED);
    }

    /**
     * Associate and existing question to and existing survey
     * 
     * @param survey
     * @param question
     * @param surveyQuestion
     * @return 
     */
    @RequestMapping(method = RequestMethod.POST, value = "/{survey}/questions/{question}")
    public ResponseEntity<Boolean> linkQuestionToSurvey( @PathVariable Long survey, @PathVariable Long question, @RequestBody SurveyQuestionDTO surveyQuestion) {
        Boolean ret;
        HttpStatus status = HttpStatus.CREATED;
        
        try {
            ret = dataService.addQuestionToSurvey(survey, question, surveyQuestion);
        }
        catch( Exception e) {
            status = HttpStatus.BAD_REQUEST;
            ret = false;
            log.error( "Error linking question to survey", e);
        }
        
        return new ResponseEntity<>(ret, status);
    }
    
    /**
     * Create a survey
     * 
     * @param survey
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/")
    public ResponseEntity<SurveyDTO> addSurvey(@RequestBody SurveyDTO survey) {
        SurveyDTO newSurvey = null;
        HttpStatus status = HttpStatus.CREATED;
        
        try {
            newSurvey = dataService.createSurvey(survey);
        }
        catch( Exception e) {
            status = HttpStatus.BAD_REQUEST;
            log.error( "Error linking question to survey", e);
        }
        
        return new ResponseEntity<>(newSurvey, HttpStatus.CREATED);
    }

    /**
     * Search for all surveys
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/")
    public ResponseEntity<List<SurveyDTO>> getAllSurveyQuestions() {
        log.debug("Get all surveys");

        List<SurveyDTO> surveyDTOList = dataService.getAllSurveys();
        return new ResponseEntity<>(surveyDTOList, HttpStatus.OK);
    }

    /**
     * Search for survey specified by survey.
     * 
     * @param survey
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RequestMapping(method = RequestMethod.GET, value = "/{survey}")
    public ResponseEntity<SurveyDTO> getSurvey(@PathVariable Long survey) {
        log.debug("Get survey: " + survey);

        SurveyDTO surveyDTO = dataService.getSurvey(survey);
        return new ResponseEntity(surveyDTO, HttpStatus.OK);
    }
    
    /**
     * Search for survey specified by surveyname.
     * 
     * @param survey
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RequestMapping(method = RequestMethod.GET, value = "/surveyByName/{surveyName}")
    public ResponseEntity<SurveyDTO> getSurvey(@PathVariable String surveyName) {
        log.debug("Get survey: " + surveyName);

        SurveyDTO surveyDTO = dataService.getSurveyByName(surveyName);
        return new ResponseEntity(surveyDTO, HttpStatus.OK);
    }

    /**
     * Search for questions/answers that are associated with the specified survey.
     * 
     * @param survey
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RequestMapping(method = RequestMethod.GET, value = "/{survey}/questions")
    public ResponseEntity<SurveyDTO> getSurveyQuestions(@PathVariable Long survey) {
        log.debug("Get survey: " + survey);

        List<SurveyQuestionDTO> sqList = dataService.getSurveyQuestions(survey);
        return new ResponseEntity(sqList, HttpStatus.OK);
    }

    /**
     * 
     * @param questionId
     * @param question
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/questions/{questionId}")
    public ResponseEntity<QuestionDTO> updateQuestion( @PathVariable Long questionId, @RequestBody QuestionDTO question) {
        HttpStatus status = HttpStatus.CREATED;
        QuestionDTO updatedQuestion = null;
        
        try {
            question.setId( questionId);
            updatedQuestion = dataService.updateQuestion(question);

            if (updatedQuestion == null) {
                status = HttpStatus.BAD_REQUEST;
            }
        }
        catch( Exception e) {
            status = HttpStatus.BAD_REQUEST;
            log.error( "Error linking question to survey", e);
        }
        
        return new ResponseEntity<>(updatedQuestion, status);
    }
    
    /**
     * Update survey 
     * @param surveyId
     * @param survey {@link SurveyDTO} containing data to update
     * @return 
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{surveyId}")
    public ResponseEntity<SurveyDTO> updateSurvey( @PathVariable Long surveyId, @RequestBody SurveyDTO survey) {
        HttpStatus status = HttpStatus.CREATED;       
        SurveyDTO updatedSurvey = null;
        
        try {
            survey.setId( surveyId);
            updatedSurvey = dataService.updateSurvey( survey);

            if( updatedSurvey == null) {
                status = HttpStatus.BAD_REQUEST;
            }
        }
        catch( Exception e) {
            status = HttpStatus.BAD_REQUEST;
            log.error( "Error linking question to survey", e);
        }
        
        return new ResponseEntity<>( updatedSurvey, status);
    }
}
