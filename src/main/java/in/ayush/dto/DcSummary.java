package in.ayush.dto;

import java.util.List;

import lombok.Data;

@Data
public class DcSummary {

	private String planName;
	private IncomeDto income;
	private EducationDto education;
	private List<ChildrenDto> children;
	

}
