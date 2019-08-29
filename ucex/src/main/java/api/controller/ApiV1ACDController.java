package api.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import api.domain_acd.AcdAgentData;
import api.domain_acd.AcdAgentNumber;
import api.domain_acd.AcdAgentNumberCommand;
import api.domain_acd.AcdRepData;
import api.domain_acd.AcdRepNumber;
import api.domain_acd.AcdRepNumberCommand;
import api.domain_acd.AcdSupervisorData;
import api.domain_acd.AcdSupervisorNumber;
import api.domain_acd.AcdSupervisorNumberCommand;
import api.domain_acd.AcdTenantNumber;
import api.domain.Rabbit;

@RestController
public class ApiV1ACDController {
/*
	private static Logger logger = LoggerFactory.getLogger(ApiV1ACDController.class);

	@Autowired
	private Rabbit r;

	// ACD 그룹 대표번호 조회
	@RequestMapping(value = "/ucex/api/v1/tenant/{tenant}/group/acd/{group_id}/rep_number", method = RequestMethod.GET)
	public AcdRepNumber viewRepNumber(@PathVariable("tenant") String tenant, @PathVariable("group_id") String group_id,
			HttpServletResponse res) throws Exception {

		logger.info("#viewRepNumber tenant:{} / group:{}", tenant, group_id);

		AcdRepNumber param = new AcdRepNumber();

		AcdRepNumberCommand command_data = new AcdRepNumberCommand();
		param.setCommand_data(command_data);

		AcdRepData data = new AcdRepData();
		command_data.setData(data);

		List<AcdTenantNumber> rep_number_list = new ArrayList<AcdTenantNumber>();
		data.setRep_number_list(rep_number_list);
		data.setRep_number_count(String.valueOf(rep_number_list.size()));

		return param;
	}

	// ACD 그룹 대표번호 추가
	@RequestMapping(value = "/ucex/api/v1/tenant/{tenant}/group/acd/{group_id}/rep_number", method = RequestMethod.POST)
	public void addRepNumber(@PathVariable("tenant") String tenant, @PathVariable("group_id") String group_id,
			@RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#addRepNumber tenant:{} / group_id:{}", tenant, group_id);
		logger.info("#addRepNumber : {}", data);

		ObjectMapper mapper = new ObjectMapper();

		AcdTenantNumber param = null;

		try {
			try {
				param = mapper.readValue(data, AcdTenantNumber.class);
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new Exception("login : Wrong JSON Format.");
			}

			//
			r.sendMessage("addRepNumber", tenant, group_id, param);
			
			res.sendError(HttpServletResponse.SC_CREATED);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}
	}

	// ACD 그룹 대표번호 삭제
	@RequestMapping(value = "/ucex/api/v1/tenant/{tenant}/group/acd/{group_id}/rep_number", method = RequestMethod.DELETE)
	public void deleteRepNumber(@PathVariable("tenant") String tenant, @PathVariable("group_id") String group_id,
			@RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#deleteRepNumber tenant:{} / group_id:{}", tenant, group_id);
		logger.info("#deleteRepNumber : {}", data);

		ObjectMapper mapper = new ObjectMapper();

		AcdTenantNumber param = null;

		try {
			try {
				param = mapper.readValue(data, AcdTenantNumber.class);
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new Exception("login : Wrong JSON Format.");
			}

			//
			r.sendMessage("deleteRepNumber", tenant, group_id, param);

			res.sendError(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}
	}

	// ACD 그룹 대표번호 전체 삭제
	@RequestMapping(value = "/ucex/api/v1/tenant/{tenant}/group/acd/{group_id}/rep_number/all", method = RequestMethod.DELETE)
	public void deleteAllRepNumber(@PathVariable("tenant") String tenant, @PathVariable("group_id") String group_id,
			HttpServletResponse res) throws Exception {

		logger.info("#deleteAllRepNumber tenant:{} / group_id:{}", tenant, group_id);

		try {

			// delete all
			r.sendMessage("deleteAllRepNumber", tenant, group_id);
			
			res.sendError(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}
	}

	// ACD 그룹 감독대 번호 조회
	@RequestMapping(value = "/ucex/api/v1/tenant/{tenant}/group/acd/{group_id}/supervisor", method = RequestMethod.GET)
	public AcdSupervisorNumber viewSupervisor(@PathVariable("tenant") String tenant, @PathVariable("group_id") String group_id,
			HttpServletResponse res) throws Exception {

		logger.info("#viewSupervisor tenant:{} / group:{}", tenant, group_id);

		AcdSupervisorNumber param = new AcdSupervisorNumber();

		AcdSupervisorNumberCommand command_data = new AcdSupervisorNumberCommand();
		param.setCommand_data(command_data);

		AcdSupervisorData data = new AcdSupervisorData();
		command_data.setData(data);

		List<AcdTenantNumber> list = new ArrayList<AcdTenantNumber>();
		data.setSupervisor_list(list);
		data.setSupervisor_count(String.valueOf(list.size()));

		return param;
	}

	// ACD 그룹 감독대 번호 추가
	@RequestMapping(value = "/ucex/api/v1/tenant/{tenant}/group/acd/{group_id}/supervisor", method = RequestMethod.POST)
	public void addSupervisor(@PathVariable("tenant") String tenant, @PathVariable("group_id") String group_id,
			@RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#addSupervisor tenant:{} / group_id:{}", tenant, group_id);
		logger.info("#addSupervisor : {}", data);

		ObjectMapper mapper = new ObjectMapper();

		AcdTenantNumber param = null;

		try {
			try {
				param = mapper.readValue(data, AcdTenantNumber.class);
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new Exception("login : Wrong JSON Format.");
			}

			//
			r.sendMessage("addSupervisor", tenant, group_id, param);

			res.sendError(HttpServletResponse.SC_CREATED);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}
	}

	// ACD 그룹 감독대 번호 삭제
	@RequestMapping(value = "/ucex/api/v1/tenant/{tenant}/group/acd/{group_id}/supervisor", method = RequestMethod.DELETE)
	public void deleteSupervisor(@PathVariable("tenant") String tenant, @PathVariable("group_id") String group_id,
			@RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#deleteSupervisor tenant:{} / group_id:{}", tenant, group_id);
		logger.info("#deleteSupervisor : {}", data);

		ObjectMapper mapper = new ObjectMapper();

		AcdTenantNumber param = null;

		try {
			try {
				param = mapper.readValue(data, AcdTenantNumber.class);
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new Exception("login : Wrong JSON Format.");
			}

			//
			r.sendMessage("deleteSupervisor", tenant, group_id, param);

			res.sendError(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}
	}

	// ACD 그룹 감독대 번호 전체 삭제
	@RequestMapping(value = "/ucex/api/v1/tenant/{tenant}/group/acd/{group_id}/supervisor/all", method = RequestMethod.DELETE)
	public void deleteAllSupervisor(@PathVariable("tenant") String tenant, @PathVariable("group_id") String group_id,
			HttpServletResponse res) throws Exception {

		logger.info("#deleteAllSupervisor tenant:{} / group_id:{}", tenant, group_id);

		try {

			// delete all
			r.sendMessage("deleteAllSupervisor", tenant, group_id);
			
			res.sendError(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}
	}

	// ACD 그룹 안내대 번호 조회
	@RequestMapping(value = "/ucex/api/v1/tenant/{tenant}/group/acd/{group_id}/agent", method = RequestMethod.GET)
	public AcdAgentNumber viewAgentNumber(@PathVariable("tenant") String tenant, @PathVariable("group_id") String group_id,
			HttpServletResponse res) throws Exception {

		logger.info("#viewAgentNumber tenant:{} / group:{}", tenant, group_id);

		AcdAgentNumber param = new AcdAgentNumber();

		AcdAgentNumberCommand command_data = new AcdAgentNumberCommand();
		param.setCommand_data(command_data);

		AcdAgentData data = new AcdAgentData();
		command_data.setData(data);

		List<AcdTenantNumber> list = new ArrayList<AcdTenantNumber>();
		data.setAgent_list(list);
		data.setAgent_count(String.valueOf(list.size()));

		return param;
	}

	// ACD 그룹 안내대 번호 추가
	@RequestMapping(value = "/ucex/api/v1/tenant/{tenant}/group/acd/{group_id}/agent", method = RequestMethod.POST)
	public void addAgentNumber(@PathVariable("tenant") String tenant, @PathVariable("group_id") String group_id,
			@RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#addAgentNumber tenant:{} / group_id:{}", tenant, group_id);
		logger.info("#addAgentNumber : {}", data);

		ObjectMapper mapper = new ObjectMapper();

		AcdTenantNumber param = null;

		try {
			try {
				param = mapper.readValue(data, AcdTenantNumber.class);
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new Exception("login : Wrong JSON Format.");
			}

			//
			r.sendMessage("addAgentNumber", tenant, group_id, param);

			res.sendError(HttpServletResponse.SC_CREATED);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}
	}

	// ACD 그룹 안내대 번호 삭제
	@RequestMapping(value = "/ucex/api/v1/tenant/{tenant}/group/acd/{group_id}/agent", method = RequestMethod.DELETE)
	public void deleteAgentNumber(@PathVariable("tenant") String tenant, @PathVariable("group_id") String group_id,
			@RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#deleteAgentNumber tenant:{} / group_id:{}", tenant, group_id);
		logger.info("#deleteAgentNumber : {}", data);

		ObjectMapper mapper = new ObjectMapper();

		AcdTenantNumber param = null;

		try {
			try {
				param = mapper.readValue(data, AcdTenantNumber.class);
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new Exception("login : Wrong JSON Format.");
			}

			//
			r.sendMessage("deleteAgentNumber", tenant, group_id, param);

			res.sendError(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}
	}

	// ACD 그룹 안내대 번호 전체 삭제
	@RequestMapping(value = "/ucex/api/v1/tenant/{tenant}/group/acd/{group_id}/agent/all", method = RequestMethod.DELETE)
	public void deleteAllAgentNumber(@PathVariable("tenant") String tenant, @PathVariable("group_id") String group_id,
			HttpServletResponse res) throws Exception {

		logger.info("#deleteAllAgentNumber tenant:{} / group_id:{}", tenant, group_id);

		try {

			// delete all
			r.sendMessage("deleteAllAgentNumber", tenant, group_id);
			
			res.sendError(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}
	}
*/
}
