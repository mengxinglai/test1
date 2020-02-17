package com.shineyue.zf.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.shineyue.calldb.util.bean.DataResult;
import com.shineyue.common.bean.BpmStateBean;
import com.shineyue.common.bean.OptLogBean;
import com.shineyue.common.bean.OptLogBeanFactory;
import com.shineyue.common.controller.CommonController;
import com.shineyue.common.dao.CommonDao;
import com.shineyue.common.service.CommonService;
import com.shineyue.common.utils.ConvertMap2Bean;
import com.shineyue.common.utils.FwjyCommonConstants;
import com.shineyue.common.utils.HttpService;
import com.shineyue.common.utils.ResourceConfig;
import com.shineyue.zf.bean.ZfBean;
import com.shineyue.zf.bean.ZfBpmBean;
import com.shineyue.zf.service.FwjyZfcfjfService;
import com.sy.register.annotation.RegisterToSMP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName FwjyZfcfjfController
 * @Description 执法查封解封
 * @Author MXL
 * @Date 2019/10/16 20:47
 **/
@RestController
public class FwjyZfcfjfController {
	@Autowired
	FwjyZfcfjfService fwjyZfcfjfService;
	@Autowired
	CommonService commonService;
	@Resource(name = "commonDao")
	CommonDao commonDao;
	@Resource(name = "commonController")
	CommonController commonController;
	@Autowired
	private ResourceConfig resource;
	@Autowired
	private ObjectMapper objectMapper;
	private static final Log LOG = LogFactory.getLog(FwjyZfcfjfController.class);
	
	/**
	 *  执法查封提交审批
	 *@param params 参数
	 *@return java.lang.String 返回数据
	 */
	@RegisterToSMP(serviceDisplay="执法查封提交审批")
	@RequestMapping(value = "FWJY/zf/cf_tjsp.service", method = RequestMethod.POST)
	public String lcfq(@RequestBody JSONObject  params) {
		LOG.info("------request param--------"+params);
		JSONObject	 httpResult= null;
		String lcms = "";
		try {		
			String channel = commonDao.getBlqd(params.getString("blqd"));
			long bpmid = params.getLong("bpmid");
			int userid = params.getInteger("userid");
			int lcbz = params.getInteger("lcbz");
			lcms = params.getString("fwzl")+"执法查封";
			Map<String, Object> map = new HashMap<String, Object>(3);
			JsonObject jsruleParams= new JsonObject(); 
			jsruleParams.addProperty("userid", userid);
			jsruleParams.addProperty("bpmid", bpmid);
			JsonObject bpmParams= new JsonObject(); 
			bpmParams.addProperty("tenantId", params.getString("zxjgbm"));
			if(lcbz == 0){
				//流程提交审批用用户
				bpmParams.addProperty("comment", " ");
				bpmParams.addProperty("applyUser", params.getString("applyUser"));
			}else{
				bpmParams.addProperty("taskId", params.getString("taskId"));
				bpmParams.addProperty("userId", params.getString("applyUser"));
				bpmParams.addProperty("comment", params.getString("comment"));
			}
			//流程描述   根据业务自己拼装
			bpmParams.addProperty("description", lcms);
			//流程图key 写死description
			bpmParams.addProperty("processKey","zf_cfsp");
			//流程businessKey  根据业务自己拼装
			bpmParams.addProperty("businessKey",bpmid+"");
			//业务地址 
			bpmParams.addProperty("url","/FWJY/zf/cf_spys.service");
			//业务渠道 前台获取
			bpmParams.addProperty("channel",channel);
			//业务名称      写死
			bpmParams.addProperty("source", "房屋交易");
			bpmParams.addProperty("lcbz",params.getInteger("lcbz"));
			bpmParams.addProperty("blqd", params.getString("blqd"));
			map.put("bpmParam", bpmParams.toString());
			map.put("businessParam", params.toString());
			map.put("ruleParam", jsruleParams.toString());
			String http = resource.getPrefixion();
			String ip = resource.getIp();
			String port =resource.getPort();
			if(lcbz == 0){
				httpResult=JSONObject.parseObject(HttpService.doPostMap(http+"://"+ip+":"+port
						+"/BPM/task/process_newStart.service", map,params));
			}else{
				httpResult=JSONObject.parseObject(HttpService.doPostMap(http+"://"+ip+":"+port
						+"/BPM/task/task_complete.service", map,params));
			}
			try{
				commonController.xjfcXxts(httpResult.toString(), "您有一笔执法查封业务待审批！");
			}catch (Exception e){
				e.printStackTrace();
			}
		} catch (Exception e) {
			httpResult = new JSONObject();
			httpResult.put("success", false);
			httpResult.put("msg", "请求流程异常");
			e.printStackTrace();
			String trace = "<font color='red'> 请求流程异常:" + e.getMessage() + "</font>";
			LOG.error(trace);
		} 
		return HttpService.setHttpResult(httpResult);
	}
	
	/**
	 * 执法查封审批
	 *@param params 参数
	 *@return java.lang.String 返回数据
	 */
	@RegisterToSMP(serviceDisplay="执法查封审批")
	@RequestMapping(value = "FWJY/zf/cf_sp.service", method = RequestMethod.POST)
	public String lccp(@RequestBody JSONObject  params) {
		JSONObject	 httpResult= null;
		String lcms = "";
		try {
			String channel = commonDao.getBlqd(params.getString("blqd"));
			long bpmid = params.getLong("bpmid");
			int userid = params.getInteger("userid");
			int lcbz = params.getInteger("lcbz");
			lcms = params.getString("fwzl")+"执法查封";
			String msg = "";
			String spyj = params.getString("comment") == null ? " " :params.getString("comment");
			Map<String, Object> map = new HashMap<String, Object>(3);
			JsonObject jsruleParams= new JsonObject();
			jsruleParams.addProperty("userid", userid);
			jsruleParams.addProperty("bpmid", bpmid);
			JsonObject bpmParams= new JsonObject(); 
			bpmParams.addProperty("tenantId", params.getString("zxjgbm"));
			bpmParams.addProperty("taskId", params.getString("taskId"));
			bpmParams.addProperty("userId", params.getString("applyUser"));
			//审批意见
			bpmParams.addProperty("comment", spyj);
			//流程描述   根据业务自己拼装
			bpmParams.addProperty("description", lcms);
			//流程图key 写死
			bpmParams.addProperty("processKey","zf_cfsp");
			//流程businessKey  根据业务自己拼装
			bpmParams.addProperty("businessKey",bpmid+"");
			//业务地址 
			bpmParams.addProperty("url","/FWJY/zf/cf_spys.service");
			//业务渠道 前台获取
			bpmParams.addProperty("channel", channel);
			//业务名称      写死
			bpmParams.addProperty("source", "房屋交易");
			bpmParams.addProperty("lcbz",params.getInteger("lcbz"));
			bpmParams.addProperty("blqd", params.getString("blqd"));
			map.put("bpmParam", bpmParams.toString());
			map.put("businessParam", params.toString());
			map.put("ruleParam", jsruleParams.toString());
			String http = resource.getPrefixion();
			String ip = resource.getIp();
			String port =resource.getPort();
			httpResult=JSONObject.parseObject(HttpService.doPostMap(http+"://"+ip+":"+port
						+"/BPM/task/task_complete.service", map,params));
			int en=3;
			if (lcbz==en){
				msg = "您有一笔执法查封业务已被退回！";
			}else{
				msg = "您有执法查封业务待审批！";
			}
			try {
				commonController.xjfcXxts(httpResult.toString(), msg);
			}catch (Exception e){
				e.printStackTrace();
			}
		} catch (Exception e) {
			httpResult = new JSONObject();
			httpResult.put("success", false);
			httpResult.put("msg", "请求流程异常");
			e.printStackTrace();
			String trace = "<font color='red'> 请求流程异常:" + e.getMessage() + "</font>";
			LOG.error(trace);
		} 
		return HttpService.setHttpResult(httpResult);
	}
	
	//c功能
public void fun3(){
	int a=1;
}

	/**
	 * 执法查封审批映射
	 *@param obj 参数
	 *@return java.util.Map<java.lang.String,java.lang.Object> 返回数据
	 */
	@WebMethod
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RegisterToSMP(serviceDisplay="执法查封审批映射")
	@RequestMapping(value = "FWJY/zf/cf_spys.service", method = RequestMethod.POST)
	public Map<String, Object> lcSpys(@RequestBody String obj)throws InterruptedException {
		Map<String, Object> enty = new HashMap<String, Object>(16);
		Map<String, Object> lh = (Map<String, Object>)JSON.parse(obj.toString());  
		Map<String, Object> businessData = null;
		Map<String, Object> bpmData = null;
		ZfBean bean = new ZfBean();
		ZfBpmBean bpmbean = new ZfBpmBean();
		OptLogBean optLogBean = OptLogBeanFactory.getOptLogBean("1002");
		try {
			businessData = JSON.parseObject(lh.get("businessParam").toString(),Map.class);
			bpmData = JSON.parseObject(lh.get("bpmParam").toString(),Map.class);
			bean=ConvertMap2Bean.convertMap(businessData, ZfBean.class);
			bpmbean=ConvertMap2Bean.convertMap(bpmData,  ZfBpmBean.class);
			String taskId = bpmbean.getTaskId();
			String businessKey = bpmbean.getBusinessKey();
			bean.setDocunid(bpmbean.getProcessInstanceId());
			bean.setTaskId(taskId);
			bean.setBusinessKey(businessKey);
			bean.setRet(0);
			bean.setIsEnd(bpmbean.getIsEnd());
			enty= fwjyZfcfjfService.zfcfSp(bean);
		} catch (Exception e) {
			e.printStackTrace();
			enty.put("success", false);
			enty.put("msg",e.getMessage());
			BpmStateBean stateBean = new BpmStateBean();
			stateBean.setId(bean.getTaskId());
			stateBean.setBusinesskey(bean.getBusinessKey());
			stateBean.setState(0);
			commonService.bpmStateAdd(stateBean);
			return enty;
		}
		try {
			boolean success = (boolean) enty.get("success");
			int lcbz = bean.getLcbz();
			if (success) {
				String czfl=" ";
			    String ywzy=" ";
				if(FwjyCommonConstants.Lcbz.SPTG == lcbz) {
					czfl="10020102";
					ywzy="执法查封审批通过 ";
				 }else if(FwjyCommonConstants.Lcbz.TH == lcbz) {
					 czfl="10020103";
					 ywzy="执法查封审批退回 ";
				 }else if(FwjyCommonConstants.Lcbz.JSLC == lcbz) {
					 czfl="10020105";
					 ywzy="执法查封审批撤销 ";
				 }else if(FwjyCommonConstants.Lcbz.ZCTJSP == lcbz) {
					 czfl="10020104";
					 ywzy="执法查封审批再次提交审批";
				 }else if(FwjyCommonConstants.Lcbz.FQLC == lcbz) {
					 czfl="10020101";
					 ywzy="执法查封发起申请 ";
				 }
				try {
					BeanUtils.copyProperties(bean,optLogBean);
					addOptLog(optLogBean, czfl, ywzy);
				}catch (Exception e){
					e.printStackTrace();
				}
			} 	
		}catch (Exception e) {
			e.printStackTrace();
			return enty;
		}
		return enty;
	}
	
	/**
	 *执法查封审批查询
	 *@param bean 执法查封bean
	 *@return com.shineyue.calldb.util.bean.DataResult 返回数据
	 */
	@RegisterToSMP(serviceDisplay="执法查封审批查询")
	@RequestMapping(value = "FWJY/zf/cfSpcx.service", method = RequestMethod.POST)
	public DataResult zfCfSpcx(@RequestBody ZfBean bean) {
		DataResult result = null;
		OptLogBean optLogBean = OptLogBeanFactory.getOptLogBean("1002");
		try {
			result = fwjyZfcfjfService.zfCfSpcx(bean);
			BeanUtils.copyProperties(bean,optLogBean);
			addOptLog(optLogBean, "10020106", "执法查封审批查询");
		} catch (Exception e) {
			return exceptionHandle("执法查封审批查询失败", e);
		}
		return result;
	}

	/**
	 * 执法期房查封网签合同号下拉模糊查询
	 *@param bean 执法查封bean
	 *@return com.shineyue.calldb.util.bean.DataResult 返回数据
	 */
	@RegisterToSMP(serviceDisplay="执法查封网签合同号下拉模糊查询")
	@RequestMapping(value = "FWJY/zf/zfcfWqhthXlcx.service", method = RequestMethod.POST)
	public DataResult zfcfWqhthXlcx(@RequestBody ZfBean bean){
		DataResult result = null;
		OptLogBean optLogBean = OptLogBeanFactory.getOptLogBean("1002");
		try{
			result = fwjyZfcfjfService.zfcfWqhthXlcx(bean);
			BeanUtils.copyProperties(bean,optLogBean);
			addOptLog(optLogBean, "10020001", "执法查封网签合同号下拉模糊查询");
		}catch (Exception e){
			return exceptionHandle("执法查封网签合同号下拉模糊查询失败", e);
		}
		return result;
	}

	/**
	 * 执法查封期房查封反显
	 *@param bean 执法查封bean
	 *@return com.shineyue.calldb.util.bean.DataResult 返回数据
	 */
	@RegisterToSMP(serviceDisplay="执法查封期房信息查询")
	@RequestMapping(value = "FWJY/zf/zfcfQfxx_cx.service", method = RequestMethod.POST)
	public DataResult zfcfQfxxcx(@RequestBody ZfBean bean) {
		DataResult result = null;
		OptLogBean optLogBean = OptLogBeanFactory.getOptLogBean("1002");
		try {
			result = fwjyZfcfjfService.zfcfQfxxcx(bean);
			BeanUtils.copyProperties(bean,optLogBean);
			addOptLog(optLogBean, "10020001", "执法查封期房信息查询");
		} catch (Exception e) {
			return exceptionHandle("执法查封期房信息查询失败", e);
		}
		return result;
	}


	/**
	 * 执法查封项目名称下拉查询
	 *@param bean 执法查封bean
	 *@return com.shineyue.calldb.util.bean.DataResult 返回数据
	 */
	@RegisterToSMP(serviceDisplay="项目名称查询")
	@RequestMapping(value = "FWJY/zf/zf_xqmccx.service", method = RequestMethod.POST)
	public DataResult zfXqmccx(@RequestBody ZfBean  bean){
		DataResult result = null;
		OptLogBean optLogBean = OptLogBeanFactory.getOptLogBean("1002");
		try {
			result = fwjyZfcfjfService.zfXqmccx(bean);
			BeanUtils.copyProperties(bean,optLogBean);
			addOptLog(optLogBean, "10020001", "执法查封期房信息查询");
		} catch (Exception e) {
			return exceptionHandle("执法查封期房信息查询失败", e);
		}
		return result;
	}

	/**
	 * 执法查封幢号下拉查询
	 *@param bean 执法查封bean
	 *@return com.shineyue.calldb.util.bean.DataResult 返回数据
	 */
	@RegisterToSMP(serviceDisplay="执法查封幢号下拉查询")
	@RequestMapping(value = "FWJY/zf/zf_zhcx.service", method = RequestMethod.POST)
	public DataResult zfZhcx(@RequestBody ZfBean  bean){
		DataResult result = null;
		OptLogBean optLogBean = OptLogBeanFactory.getOptLogBean("1002");
		try {
			result=fwjyZfcfjfService.zfZhcx(bean);
			BeanUtils.copyProperties(bean,optLogBean);
			addOptLog(optLogBean, "10020001", "执法查封幢号下拉查询");
		} catch (Exception e) {
			return exceptionHandle("执法查封幢号下拉查询", e);
		}
		return result;
	}

	/**
	 * 执法查封房间号下拉查询
	 *@param bean 执法查封bean
	 *@return com.shineyue.calldb.util.bean.DataResult 返回数据
	 */
	@RegisterToSMP(serviceDisplay="执法查封房间号查询")
	@RequestMapping(value = "FWJY/zf/zf_fjhcx.service", method = RequestMethod.POST)
	public DataResult zfFjhcx(@RequestBody ZfBean  bean){
		DataResult result = null;
		OptLogBean optLogBean = OptLogBeanFactory.getOptLogBean("1002");
		try {
			result = fwjyZfcfjfService.zfFjhcx(bean);
			BeanUtils.copyProperties(bean,optLogBean);
			addOptLog(optLogBean, "10020001", "执法查封房间号查询");
		} catch (Exception e) {
			return exceptionHandle("执法查封房间号查询失败", e);
		}
		return result;
	}


	/**
	 * 执法解封提交审批
	 *@param params 参数
	 *@return java.lang.String 返回数据
	 */
	@RegisterToSMP(serviceDisplay="执法解封提交审批")
	@RequestMapping(value = "FWJY/zf/jf_tjsp.service", method = RequestMethod.POST)
	public String lcfqjf(@RequestBody JSONObject  params) {
		LOG.info("------request param--------"+params);
		JSONObject	 httpResult= null;
		try {
			String channel = commonDao.getBlqd(params.getString("blqd"));
			long bpmid = params.getLong("bpmid");
			int userid = params.getInteger("userid");
			int lcbz = params.getInteger("lcbz");
			String lcms = params.getString("fwzl")+"执法解封";
			Map<String, Object> map = new HashMap<String, Object>(3);
			JsonObject jsruleParams= new JsonObject();
			jsruleParams.addProperty("userid", userid);
			jsruleParams.addProperty("bpmid", bpmid);
			JsonObject bpmParams= new JsonObject();
			bpmParams.addProperty("tenantId", params.getString("zxjgbm"));
			if(lcbz == 0){
				//流程提交审批用用户
				bpmParams.addProperty("comment", " ");
				bpmParams.addProperty("applyUser", params.getString("applyUser"));
			}else{
				bpmParams.addProperty("taskId", params.getString("taskId"));
				bpmParams.addProperty("userId", params.getString("applyUser"));
				bpmParams.addProperty("comment", params.getString("comment"));
			}
			//流程描述   根据业务自己拼装
			bpmParams.addProperty("description", lcms);
			//流程图key 写死description
			bpmParams.addProperty("processKey","zf_jfsp");
			//流程businessKey  根据业务自己拼装
			bpmParams.addProperty("businessKey",bpmid+"");
			//业务地址
			bpmParams.addProperty("url","/FWJY/zf/jf_spys.service");
			//业务渠道 前台获取
			bpmParams.addProperty("channel",channel);
			//业务名称      写死
			bpmParams.addProperty("source", "房屋交易");
			bpmParams.addProperty("lcbz",params.getInteger("lcbz"));
			bpmParams.addProperty("blqd", params.getString("blqd"));
			map.put("bpmParam", bpmParams.toString());
			map.put("businessParam", params.toString());
			map.put("ruleParam", jsruleParams.toString());
			String http = resource.getPrefixion();
			String ip = resource.getIp();
			String port =resource.getPort();
			if(lcbz == 0){
				httpResult=JSONObject.parseObject(HttpService.doPostMap(http+"://"+ip+":"+port
						+"/BPM/task/process_newStart.service", map,params));
			}else{
				httpResult=JSONObject.parseObject(HttpService.doPostMap(http+"://"+ip+":"+port
						+"/BPM/task/task_complete.service", map,params));
			}
			try {
				commonController.xjfcXxts(httpResult.toString(), "您有一笔执法解封业务待审批！");
			}catch (Exception e){
				e.printStackTrace();
			}
		} catch (Exception e) {
			httpResult = new JSONObject();
			httpResult.put("success", false);
			httpResult.put("msg", "请求流程异常");
			e.printStackTrace();
			String trace = "<font color='red'> 请求流程异常:" + e.getMessage() + "</font>";
			LOG.error(trace);
		}
		return HttpService.setHttpResult(httpResult);
	}

	/**
	 * 执法解封审批
	 *@param params 参数
	 *@return java.lang.String 返回数据
	 */
	@RegisterToSMP(serviceDisplay="执法解封审批")
	@RequestMapping(value = "FWJY/zf/jf_sp.service", method = RequestMethod.POST)
	public String lccpjf(@RequestBody JSONObject  params) {
		JSONObject	 httpResult= null;
		String qymc = params.getString("qymc");
		try {
			String channel = commonDao.getBlqd(params.getString("blqd"));
			long bpmid = params.getLong("bpmid");
			int userid = params.getInteger("userid");
			int lcbz = params.getInteger("lcbz");
			String lcms = params.getString("fwzl")+"执法解封";
			String msg = "";
			String spyj = params.getString("comment") == null ? " " :params.getString("comment");
			Map<String, Object> map = new HashMap<String, Object>(3);
			JsonObject jsruleParams= new JsonObject();
			jsruleParams.addProperty("userid", userid);
			jsruleParams.addProperty("bpmid", bpmid);
			JsonObject bpmParams= new JsonObject();
			bpmParams.addProperty("tenantId", params.getString("zxjgbm"));
			bpmParams.addProperty("taskId", params.getString("taskId"));
			bpmParams.addProperty("userId", params.getString("applyUser"));
			//审批意见
			bpmParams.addProperty("comment", spyj);
			//流程描述   根据业务自己拼装
			bpmParams.addProperty("description", lcms);
			//流程图key 写死
			bpmParams.addProperty("processKey","zf_jfsp");
			//流程businessKey  根据业务自己拼装
			bpmParams.addProperty("businessKey",bpmid+"");
			//业务地址
			bpmParams.addProperty("url","/FWJY/zf/jf_spys.service");
			//业务渠道 前台获取
			bpmParams.addProperty("channel", channel);
			//业务名称      写死
			bpmParams.addProperty("source", "房屋交易");
			bpmParams.addProperty("lcbz",params.getInteger("lcbz"));
			bpmParams.addProperty("blqd", params.getString("blqd"));
			map.put("bpmParam", bpmParams.toString());
			map.put("businessParam", params.toString());
			map.put("ruleParam", jsruleParams.toString());
			String http = resource.getPrefixion();
			String ip = resource.getIp();
			String port =resource.getPort();
			httpResult=JSONObject.parseObject(HttpService.doPostMap(http+"://"+ip+":"+port
					+"/BPM/task/task_complete.service", map,params));
			int en=3;
			if (lcbz==en){
				msg = "您有一笔执法解封业务已被退回！";
			}else{
				msg = "您有一笔执法解封业务待审批！";
			}
			try {
				commonController.xjfcXxts(httpResult.toString(), msg);
			}catch (Exception e){
				e.printStackTrace();
			}
		} catch (Exception e) {
			httpResult = new JSONObject();
			httpResult.put("success", false);
			httpResult.put("msg", "请求流程异常");
			e.printStackTrace();
			String trace = "<font color='red'> 请求流程异常:" + e.getMessage() + "</font>";
			LOG.error(trace);
		}
		return HttpService.setHttpResult(httpResult);
	}

	/**
	 * 执法解封完成办理
	 *@param obj 参数
	 *@return java.util.Map<java.lang.String,java.lang.Object> 返回数据
	 */
	@WebMethod
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RegisterToSMP(serviceDisplay="解封审批映射")
	@RequestMapping(value = "FWJY/zf/jf_spys.service", method = RequestMethod.POST)
	public Map<String, Object> lcSpysjf(@RequestBody String obj)throws InterruptedException {
		Map<String, Object> enty = new HashMap<String, Object>(16);
		Map<String, Object> lh = (Map<String, Object>)JSON.parse(obj.toString());
		Map<String, Object> businessData = null;
		Map<String, Object> bpmData = null;
		ZfBean bean = new ZfBean();
		ZfBpmBean bpmbean = new ZfBpmBean();
		OptLogBean optLogBean = OptLogBeanFactory.getOptLogBean("1002");
		try {
			businessData = JSON.parseObject(lh.get("businessParam").toString(),Map.class);
			bpmData = JSON.parseObject(lh.get("bpmParam").toString(),Map.class);
			bean=ConvertMap2Bean.convertMap(businessData, ZfBean.class);
			bpmbean=ConvertMap2Bean.convertMap(bpmData,  ZfBpmBean.class);
			String taskId = bpmbean.getTaskId();
			String businessKey = bpmbean.getBusinessKey();
			bean.setDocunid(bpmbean.getProcessInstanceId());
			bean.setTaskId(taskId);
			bean.setBusinessKey(businessKey);
			bean.setRet(0);
			bean.setIsEnd(bpmbean.getIsEnd());
			enty= fwjyZfcfjfService.zfjfSp(bean);
		} catch (Exception e) {
			e.printStackTrace();
			enty.put("success", false);
			enty.put("msg",e.getMessage());
			BpmStateBean stateBean = new BpmStateBean();
			stateBean.setId(bean.getTaskId());
			stateBean.setBusinesskey(bean.getBusinessKey());
			stateBean.setState(0);
			commonService.bpmStateAdd(stateBean);
			return enty;
		}
		try {
			boolean success = (boolean) enty.get("success");
			int lcbz = bean.getLcbz();
			if (success) {
				String czfl=" ";
				String ywzy=" ";
				if(FwjyCommonConstants.Lcbz.SPTG == lcbz) {
					czfl="10020202";
					ywzy="执法解封审批通过 ";
				}else if(FwjyCommonConstants.Lcbz.TH == lcbz) {
					czfl="10020203";
					ywzy="执法解封审批拒绝";
				}else if(FwjyCommonConstants.Lcbz.JSLC == lcbz) {
					czfl="10020205";
					ywzy="执法解封审批撤销 ";
				}else if(FwjyCommonConstants.Lcbz.ZCTJSP == lcbz) {
					czfl="10020204";
					ywzy="执法解封审批修改重发";
				}else if(FwjyCommonConstants.Lcbz.FQLC == lcbz) {
					czfl="10020201";
					ywzy="执法解封提交审批 ";
				}
				try {
					BeanUtils.copyProperties(bean,optLogBean);
					addOptLog(optLogBean, czfl, ywzy);
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			return enty;
		}
		return enty;
	}


	/**
	 * 执法解封审批查询
	 *@param bean 执法查封bean
	 *@return com.shineyue.calldb.util.bean.DataResult 返回数据
	 */
	@RegisterToSMP(serviceDisplay="执法解封审批查询")
	@RequestMapping(value = "FWJY/zf/jfspcx.service", method = RequestMethod.POST)
	public DataResult zfJfspcx(@RequestBody ZfBean bean) {
		DataResult result = null;
		OptLogBean optLogBean = OptLogBeanFactory.getOptLogBean("1002");
		try {
			result = fwjyZfcfjfService.zfJfSpcx(bean);
			BeanUtils.copyProperties(bean,optLogBean);
			addOptLog(optLogBean, "10020001", "执法解封审批查询");
		} catch (Exception e) {
			return exceptionHandle("执法解封审批查询失败", e);
		}
		return result;
	}

	/**
	 * 现房查封房屋坐落/地址下拉模糊查询
	 *@param bean 执法查封bean
	 *@return com.shineyue.calldb.util.bean.DataResult	返回数据
	 */
	@RegisterToSMP(serviceDisplay="现房查封房屋坐落/地址下拉模糊查询")
	@RequestMapping(value = "FWJY/zf/zfcfXffwzlCx.service", method = RequestMethod.POST)
	public DataResult zfcfXffwzlCx(@RequestBody ZfBean bean) {
		DataResult result = null;
		OptLogBean optLogBean = OptLogBeanFactory.getOptLogBean("1002");
		try {
			result = fwjyZfcfjfService.zfcfXffwzlCx(bean);
			BeanUtils.copyProperties(bean,optLogBean);
			addOptLog(optLogBean, "10020001", "现房查封房屋坐落/地址下拉模糊查询");
		} catch (Exception e) {
			return exceptionHandle("现房查封房屋坐落/地址下拉模糊查询失败", e);
		}
		return result;
	}

	/**
	 * 执法查封期房清册查询
	 *@param bean 执法bean
	 *@return com.shineyue.calldb.util.bean.DataResult 返回数据
	 */
	@RegisterToSMP(serviceDisplay="执法查封期房清册查询")
	@RequestMapping(value = "FWJY/zf/zfcfQfQcCx.service", method = RequestMethod.POST)
	public DataResult zfcfQfQcCx(@RequestBody ZfBean bean) {
		DataResult result = null;
		OptLogBean optLogBean = OptLogBeanFactory.getOptLogBean("1002");
		try {
			result = fwjyZfcfjfService.zfcfQfQcCx(bean);
			BeanUtils.copyProperties(bean,optLogBean);
			addOptLog(optLogBean, "10020001", "执法查封期房清册查询");
		} catch (Exception e) {
			return exceptionHandle("执法查封期房清册查询失败", e);
		}
		return result;
	}

	/**
	 * 执法查封单元号查询
	 *@param bean 执法查封bean
	 *@return com.shineyue.calldb.util.bean.DataResult 返回数据
	 */
    @RegisterToSMP(serviceDisplay="执法查封单元号查询")
    @RequestMapping(value = "FWJY/zf/zfcfDycx.service", method = RequestMethod.POST)
	public DataResult zfcfDyCx(@RequestBody ZfBean bean){
        DataResult result = null;
		OptLogBean optLogBean = OptLogBeanFactory.getOptLogBean("1002");
        try{
            result = fwjyZfcfjfService.zfcfDyCx(bean);
			BeanUtils.copyProperties(bean,optLogBean);
			addOptLog(optLogBean, "10020001", "执法查封单元号查询");
        }catch (Exception e){
			return exceptionHandle("执法查封单元号查询失败", e);
        }
        return result;
    }

    /**
     * 执法查封现房房屋所有权证号下拉模糊查询
     *@param bean 执法查封bean
     *@return com.shineyue.calldb.util.bean.DataResult 返回数据
     */
	@RegisterToSMP(serviceDisplay="执法查封现房房屋所有权证号查询")
    @RequestMapping(value = "FWJY/zf/zfcfXffwsyqzhCx.service",method = RequestMethod.POST)
    public DataResult zfcfXffwsyqzhCx(@RequestBody ZfBean bean){
		DataResult result = null;
		OptLogBean optLogBean = OptLogBeanFactory.getOptLogBean("1002");
		try{
			result = fwjyZfcfjfService.zfcfXffwsyqzhCx(bean);
			BeanUtils.copyProperties(bean,optLogBean);
			addOptLog(optLogBean, "10020001", "执法查封现房房屋所有权证号查询");
		}catch (Exception e){
			return exceptionHandle("执法查封现房房屋所有权证号查询失败", e);
		}
		return result;
	}

	/**
	 * 执法查封现房权利人查询
	 *@param bean 执法bean
	 *@return com.shineyue.calldb.util.bean.DataResult 返回数据
	 */
	@RegisterToSMP(serviceDisplay="执法查封现房权利人查询")
	@RequestMapping(value = "FWJY/zf/zfcfXfqlrCx.service",method = RequestMethod.POST)
	public DataResult zfcfXfqlrCx(@RequestBody ZfBean bean){
		DataResult result = null;
		OptLogBean optLogBean = OptLogBeanFactory.getOptLogBean("1002");
		try{
			result = fwjyZfcfjfService.zfcfXfqlrCx(bean);
			BeanUtils.copyProperties(bean,optLogBean);
			addOptLog(optLogBean, "10020001", "执法查封现房权利人查询");
		}catch (Exception e){
			return exceptionHandle("执法查封现房权利人查询失败", e);
		}
		return result;
	}

	/**
	 * 执法查封现房清册查询
	 *@param bean 执法查封bean
	 *@return com.shineyue.calldb.util.bean.DataResult 返回数据
	 */
	@RegisterToSMP(serviceDisplay="执法查封现房清册查询")
	@RequestMapping(value = "FWJY/zf/zfcfXfQcCx.service", method = RequestMethod.POST)
	public DataResult zfcfXfQcCx(@RequestBody ZfBean bean) {
		DataResult result = null;
		OptLogBean optLogBean = OptLogBeanFactory.getOptLogBean("1002");
		try {
			result = fwjyZfcfjfService.zfcfXfQcCx(bean);
			BeanUtils.copyProperties(bean,optLogBean);
			addOptLog(optLogBean, "10020001", "执法查封现房清册查询");
		} catch (Exception e) {
			return exceptionHandle("执法查封现房清册查询失败", e);
		}
		return result;
	}

	/**
	 * 执法查封现房查封信息反显
	 *@param bean 执法查封bean
	 *@return com.shineyue.calldb.util.bean.DataResult 返回数据
	 */
	@RegisterToSMP(serviceDisplay="执法查封现房查封信息反显查询")
	@RequestMapping(value = "FWJY/zf/zfcfXfxx_cx.service", method = RequestMethod.POST)
	public DataResult zfcfXfxxcx(@RequestBody ZfBean bean){
		DataResult result = null;
		OptLogBean optLogBean = OptLogBeanFactory.getOptLogBean("1002");
		try{
			result = fwjyZfcfjfService.zfcfXfxxcx(bean);
			BeanUtils.copyProperties(bean,optLogBean);
			addOptLog(optLogBean, "10020001", "执法查封现房查封信息反显查询");
		}catch (Exception e){
			return exceptionHandle("执法查封现房查封信息反显查询失败", e);
		}
		return  result;
	}

	/**
	 * 解封执法单位名称查询
	 *@param bean 执法查封bean
	 *@return com.shineyue.calldb.util.bean.DataResult 返回数据
	 */
	@RegisterToSMP(serviceDisplay = "解封执法单位名称查询")
	@RequestMapping(value = "FWJY/zf/zfcfZfdwmcCx.service",method = RequestMethod.POST)
	public DataResult zfcfZfdwmcCx(@RequestBody ZfBean bean){
		DataResult result = null;
		OptLogBean optLogBean = OptLogBeanFactory.getOptLogBean("1002");
		try{
			result = fwjyZfcfjfService.zfcfZfdwmcCx(bean);
			BeanUtils.copyProperties(bean,optLogBean);
			addOptLog(optLogBean, "10020001", "解封执法单位名称查询");
		}catch (Exception e){
			return exceptionHandle("解封执法单位名称查询失败", e);
		}
		return result;
	}

	/**
	 * 执法查封解封清册查询
	 *@param bean 执法查封bean
	 *@return com.shineyue.calldb.util.bean.DataResult 返回数据
	 */
	@RegisterToSMP(serviceDisplay="执法查封解封清册查询")
	@RequestMapping(value = "FWJY/zf/zfcfJfQcCx.service", method = RequestMethod.POST)
	public DataResult zfcfJfQcCx(@RequestBody ZfBean bean) {
		DataResult result = null;
		OptLogBean optLogBean = OptLogBeanFactory.getOptLogBean("1002");
		try {
			result = fwjyZfcfjfService.zfcfJfQcCx(bean);
			BeanUtils.copyProperties(bean,optLogBean);
			addOptLog(optLogBean, "10020001", "执法查封解封清册查询");
		} catch (Exception e) {
			return exceptionHandle("执法查封解封清册查询失败", e);
		}
		return result;
	}

	/**
	 * @param optLogBean 日志Bean
	 * @param czfl       操作分类
	 * @param ywzy       业务摘要
	 * @return void 空
	 * @date 2019/12/17
	 */
	public void addOptLog(OptLogBean optLogBean, String czfl, String ywzy) {
		try {
			LOG.fatal(optLogBean.toString());
			optLogBean.setYwzy(ywzy);
			optLogBean.setYwfl("01");
			optLogBean.setYwlx("1002");
			optLogBean.setCzfl(czfl);
			optLogBean.setYwczje(0);
			optLogBean.setYwczbs(0);
			optLogBean.setYwlsh(optLogBean.getYwlsh() == null ? "" : optLogBean.getYwlsh());
			optLogBean.setZxjgbm(optLogBean.getZxjgbm() == null ? (optLogBean.getJgbm() == null ? "" : optLogBean.getJgbm()) : optLogBean.getZxjgbm());
			commonService.optLogAdd(optLogBean);
		} catch (Exception e) {
			LOG.error("<font color='red'> 添加日志失败,异常为：" + e.getMessage() + "</font>");
		}
	}

	/**
	 * 异常处理
	 *
	 * @param ywzy 业务摘要
	 * @param e    异常
	 * @return com.shineyue.calldb.util.bean.DataResult 返回数据
	 * @date 2019/12/17
	 */
	public DataResult exceptionHandle(String ywzy, Exception e) {
		DataResult dataResult = new DataResult();
		dataResult.setSuccess(false);
		dataResult.setMsg(ywzy);
		LOG.error("<font color='red'> " + ywzy + ",异常为：" + e.getMessage() + "</font>");
		return dataResult;
	}

}
