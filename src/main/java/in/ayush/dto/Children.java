package in.ayush.dto;

import java.util.List;

import lombok.Data;

@Data
public class Children {

	private Long caseNum;

	private List<ChildrenDto> children;

}
