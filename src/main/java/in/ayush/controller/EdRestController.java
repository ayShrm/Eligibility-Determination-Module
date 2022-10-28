package in.ayush.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import in.ayush.dto.EligResponse;
import in.ayush.service.EdEligService;

@RestController
public class EdRestController {

	@Autowired
	private EdEligService service;

	@GetMapping("/checkeligibility")
	public ResponseEntity<EligResponse> getEligDtls(Long caseNum) {
		EligResponse response = service.determineElig(caseNum);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
