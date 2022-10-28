package in.ayush.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.ayush.entity.CitizenAppEntity;
import in.ayush.entity.CoTriggerEntity;
import in.ayush.entity.DcCasesEntity;
import in.ayush.entity.DcChildrenEntity;
import in.ayush.entity.DcEducationEntity;
import in.ayush.entity.DcIncomeEntity;
import in.ayush.entity.EdEligDtls;
import in.ayush.entity.PlanEntity;
import in.ayush.repository.CoTriggerRepo;
import in.ayush.repository.DcCasesRepo;
import in.ayush.repository.DcChildrenRepo;
import in.ayush.repository.DcEducationRepo;
import in.ayush.repository.DcIncomeRepo;
import in.ayush.repository.EdEligRepo;
import in.ayush.repository.PlanRepo;
import in.ayush.response.EligResponse;

@Service
public class EdEligServiceImpl implements EdEligService {

	@Autowired
	private DcCasesRepo caseRepo;

	@Autowired
	private PlanRepo planRepo;

	@Autowired
	private DcIncomeRepo incomeRepo;

	@Autowired
	private EdEligRepo edRepo;

	@Autowired
	private DcChildrenRepo childRepo;

	@Autowired
	private DcEducationRepo eduRepo;

	@Autowired
	private CoTriggerRepo coRepo;

	@Override
	public EligResponse determineElig(Long caseNum) {

		Optional<DcCasesEntity> caseEntity = caseRepo.findById(caseNum);
		Integer planId = null;
		String planName = null;

		CitizenAppEntity appEntity = caseEntity.get().getAppEntity();
		String fullName = appEntity.getFullName();
		Long ssn = appEntity.getSsn();
		LocalDate dob = appEntity.getDob();

		if (caseEntity.isPresent()) {
			PlanEntity planEntity = caseEntity.get().getPlanEntity();
			planId = planEntity.getPlanId();
		}

		Optional<PlanEntity> planEntity = planRepo.findById(planId);
		if (planEntity.isPresent()) {
			PlanEntity plan = planEntity.get();
			planName = plan.getPlanName();
		}

		EligResponse eligResponse = executePlanCondition(caseNum, planName, dob);

		EdEligDtls eligEntity = new EdEligDtls(caseEntity.get());
		BeanUtils.copyProperties(eligResponse, eligEntity);
		eligEntity.setHolderName(fullName);
		eligEntity.setSsn(ssn);
		edRepo.save(eligEntity);

		CoTriggerEntity coEntity = new CoTriggerEntity();
		coEntity.setCasesEntity(caseEntity.get());
		coEntity.setTrgStatus("Pending");
		coRepo.save(coEntity);

		return eligResponse;
	}

	private EligResponse executePlanCondition(Long caseNum, String planName, LocalDate dob) {

		EligResponse response = new EligResponse();
		response.setPlanName(planName);

		Optional<DcIncomeEntity> incomeEntity = incomeRepo.findByCaseNum(caseNum);
		Double empIncome = incomeEntity.get().getMonthlySalaryIncome();

		if (planName.equals("SNAP")) {

			if (empIncome <= 300) {
				response.setPlanStatus("Approved");

			} else {
				response.setPlanStatus("Denied");
				response.setDenialReason("High Income");
			}

		} else if (planName.equals("CCAP")) {

			boolean ageCondition = true;
			boolean kidsCount = false;

			List<Optional<DcChildrenEntity>> children = childRepo.findByCaseNum(caseNum);
			if (!children.isEmpty()) {
				kidsCount = true;
				for (Optional<DcChildrenEntity> entity : children) {
					Integer childAge = entity.get().getChildrenAge();
					if (childAge > 16) {
						ageCondition = false;
						break;
					}
				}
			}

			if (empIncome <= 300 && kidsCount && ageCondition) {
				response.setPlanStatus("Approved");
			} else {
				response.setPlanStatus("Denied");
				response.setDenialReason("Plan conditions not satisfied!!!!");
			}

		} else if (planName.equals("Medicare")) {

			int age = calculateAge(dob);
			if (age >= 65) {
				response.setPlanStatus("Approved");

			} else {
				response.setPlanStatus("Denied");
				response.setDenialReason("Age condition not satisfied");
			}

		} else if (planName.equals("Medicaid")) {

			Double propertyIncome = incomeEntity.get().getPropertyIcome();

			if (empIncome <= 300 && propertyIncome == 0) {
				response.setPlanStatus("Approved");
			} else {
				response.setPlanStatus("Denied");
				response.setDenialReason("High Income..!!");
			}

		} else if (planName.equals("NJW")) {

			Optional<DcEducationEntity> eduEntity = eduRepo.findByCaseNum(caseNum);
			Integer year = eduEntity.get().getGraduationYear();
			int currentYear = LocalDate.now().getYear();

			if (empIncome <= 0 && year < currentYear) {
				response.setPlanStatus("Approved");
			} else {
				response.setPlanStatus("Denied");
				response.setDenialReason("Plan conditin not satisfied..!!");
			}
		}

		if (response.getPlanStatus().equals("Approved")) {
			response.setPlanStartDate(LocalDate.now());
			response.setPlanEndDate(LocalDate.now().plusMonths(12));
			response.setBenefitAmt(350.00);
		}

		return response;
	}

	private int calculateAge(LocalDate birthDate) {
		LocalDate currentDate = java.time.LocalDate.now();
		if ((birthDate != null)) {
			return Period.between(birthDate, currentDate).getYears();
		} else {
			return 0;
		}
	}
}
