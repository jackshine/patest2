package com.csswust.patest2.controller.lexam;

import com.csswust.patest2.common.APIResult;
import com.csswust.patest2.common.config.Config;
import com.csswust.patest2.common.config.SiteKey;
import com.csswust.patest2.controller.common.BaseAction;
import com.csswust.patest2.dao.SubmitInfoDao;
import com.csswust.patest2.dao.SubmitSimilarityDao;
import com.csswust.patest2.dao.UserInfoDao;
import com.csswust.patest2.dao.UserProfileDao;
import com.csswust.patest2.dao.common.BaseDao;
import com.csswust.patest2.dao.common.BaseQuery;
import com.csswust.patest2.entity.SubmitInfo;
import com.csswust.patest2.entity.SubmitSimilarity;
import com.csswust.patest2.entity.UserInfo;
import com.csswust.patest2.entity.UserProfile;
import com.csswust.patest2.service.sim.SimInput;
import com.csswust.patest2.service.sim.SimOutput;
import com.csswust.patest2.service.sim.SimResult;
import com.csswust.patest2.service.sim.SimService;
import com.csswust.patest2.utils.FileUtil;
import com.csswust.patest2.utils.SimHash;
import com.csswust.patest2.utils.StreamUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.csswust.patest2.service.common.BatchQueryService.getFieldByList;
import static com.csswust.patest2.service.common.BatchQueryService.selectRecordByIds;

/**
 * Created by 972536780 on 2018/3/24.
 */
@Validated
@RestController
@RequestMapping("/submitSimilarity")
public class SubmitSimilarityAction extends BaseAction {
    private static Logger log = LoggerFactory.getLogger(SubmitSimilarityAction.class);

    @Autowired
    private SubmitSimilarityDao submitSimilarityDao;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private SubmitInfoDao submitInfoDao;
    @Autowired
    private UserProfileDao userProfileDao;
    @Autowired
    private SimService simService;

    @RequestMapping(value = "/selectByCondition", method = {RequestMethod.GET, RequestMethod.POST})
    public Map<String, Object> selectByCondition(
            SubmitSimilarity submitSimilarity,
            @RequestParam(required = false) Double lowerLimit,
            @RequestParam(required = false) Double upperLimit,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer rows) {
        Map<String, Object> res = new HashMap<>();
        BaseQuery baseQuery = new BaseQuery();
        baseQuery.setCustom("lowerLimit", lowerLimit);
        baseQuery.setCustom("upperLimit", upperLimit);
        if (StringUtils.isNotBlank(userName)) {
            UserInfo userInfo = userInfoDao.selectByUsername(userName);
            if (userInfo != null) {
                SubmitInfo submitInfo = new SubmitInfo();
                submitInfo.setUserId(userInfo.getUserId());
                List<SubmitInfo> submitInfoList = submitInfoDao.selectByCondition(submitInfo, new BaseQuery());
                List<Integer> submId = getFieldByList(submitInfoList, "submId", SubmitInfo.class);
                baseQuery.setCustom("submitIds1", submId);
            }
        }
        Integer total = submitSimilarityDao.selectByConditionGetCount(submitSimilarity, baseQuery);
        baseQuery.setPageRows(page, rows);
        List<SubmitSimilarity> submitSimilarityList = submitSimilarityDao.selectByCondition(submitSimilarity, baseQuery);
        List<SubmitInfo> submitInfoList1 = selectRecordByIds(
                getFieldByList(submitSimilarityList, "submitId1", SubmitSimilarity.class),
                "submId", (BaseDao) submitInfoDao, SubmitInfo.class);
        List<UserInfo> userInfoList1 = selectRecordByIds(
                getFieldByList(submitInfoList1, "userId", SubmitInfo.class),
                "userId", (BaseDao) userInfoDao, UserInfo.class);
        List<UserProfile> userProfileList1 = selectRecordByIds(
                getFieldByList(userInfoList1, "userProfileId", UserInfo.class),
                "useProId", (BaseDao) userProfileDao, UserProfile.class);

        List<SubmitInfo> submitInfoList2 = selectRecordByIds(
                getFieldByList(submitSimilarityList, "submitId2", SubmitSimilarity.class),
                "submId", (BaseDao) submitInfoDao, SubmitInfo.class);
        List<UserInfo> userInfoList2 = selectRecordByIds(
                getFieldByList(submitInfoList2, "userId", SubmitInfo.class),
                "userId", (BaseDao) userInfoDao, UserInfo.class);
        List<UserProfile> userProfileList2 = selectRecordByIds(
                getFieldByList(userInfoList2, "userProfileId", UserInfo.class),
                "useProId", (BaseDao) userProfileDao, UserProfile.class);
        res.put("total", total);
        res.put("submitSimilarityList", submitSimilarityList);
        res.put("submitInfoList1", submitInfoList1);
        res.put("submitInfoList2", submitInfoList2);
        res.put("userInfoList1", userInfoList1);
        res.put("userProfileList1", userProfileList1);
        res.put("userInfoList2", userInfoList2);
        res.put("userProfileList2", userProfileList2);
        return res;
    }

    @RequestMapping(value = "/getSimByProbId", method = {RequestMethod.GET, RequestMethod.POST})
    public Object getSimByProbId(
            @RequestParam Integer examId,
            @RequestParam Integer submId) {
        APIResult apiResult = new APIResult();
        if (examId == null || submId == null) return apiResult;
        SubmitInfo currSubmitInfo = submitInfoDao.selectByPrimaryKey(submId);
        if (currSubmitInfo == null) return apiResult;
        if (currSubmitInfo.getProblemId() == null) return apiResult;
        if (currSubmitInfo.getJudgerId() == null) return apiResult;
        if (currSubmitInfo.getUserId() == null) return apiResult;
        Integer judgerId = currSubmitInfo.getJudgerId();

        // 判断该提交是否已经计算过相似度
        SubmitSimilarity temp = new SubmitSimilarity();
        temp.setSubmitId1(submId);
        int total = submitSimilarityDao.selectByConditionGetCount(temp, new BaseQuery());
        if (total > 0) {
            apiResult.setStatusAndDesc(-500, "不能重复计算相似度");
            return apiResult;
        }
        // 查询属于同一个题目下,同一种语言的所有提交
        SubmitInfo submitInfo = new SubmitInfo();
        submitInfo.setExamId(examId);
        submitInfo.setProblemId(currSubmitInfo.getProblemId());
        submitInfo.setJudgerId(judgerId);
        int testMaxNum = Config.getToInt(SiteKey.SIM_TEST_DATA_MAX_NUM,
                SiteKey.SIM_TEST_DATA_MAX_NUM_DE);
        List<SubmitInfo> submitInfoList = submitInfoDao.selectByCondition(submitInfo,
                new BaseQuery(1, testMaxNum));
        if (submitInfoList == null || submitInfoList.size() == 0) {
            apiResult.setStatusAndDesc(-1, "本提交无提交数据");
            return apiResult;
        }
        SimInput simInput = new SimInput();
        List<SubmitInfo> leftList = new ArrayList<>();
        List<SubmitInfo> rightList = new ArrayList<>();
        leftList.add(currSubmitInfo);

        // 计算相似度
        int count = 0, length = submitInfoList.size();
        for (int i = 0; i < length; i++) {
            SubmitInfo subInfo2 = submitInfoList.get(i);
            // 同一个人submId
            if (subInfo2 == null) continue;
            if (subInfo2.getSubmId() == null) continue;
            if (subInfo2.getUserId() == null) continue;
            if (submId.intValue() == subInfo2.getSubmId().intValue()) continue;
            if (currSubmitInfo.getUserId().intValue() == subInfo2.getUserId().intValue()) continue;
            rightList.add(subInfo2);
        }
        if (rightList.size() == 0) {
            apiResult.setStatusAndDesc(-2, "本提交无参考数据");
            return apiResult;
        }
        simInput.setLeftCodeList(leftList);
        simInput.setRightCodeList(rightList);
        String scriptPath = Config.get(SiteKey.SIM_SCRIPT_PATH, SiteKey.SIM_SCRIPT_PATH_DE);
        if (judgerId == 1) scriptPath = scriptPath + "/sim_c";
        else if (judgerId == 2) scriptPath = scriptPath + "/sim_c++";
        else if (judgerId == 3) scriptPath = scriptPath + "/sim_java";
        else scriptPath = scriptPath + "/sim_text.exe";
        simInput.setScriptPath(scriptPath);
        SimOutput simOutput = simService.judge(simInput);
        if (simOutput.getError() != null) {
            apiResult.setStatusAndDesc(-3, simOutput.getError());
            return apiResult;
        }
        List<SimResult> simResultList = simOutput.getSimResultList();
        if (simResultList == null || simResultList.size() == 0) {
            apiResult.setStatusAndDesc(-4, "相识度计算无结果");
            return apiResult;
        }
        int saveMaxNum = Config.getToInt(SiteKey.SIM_SAVE_DATABASE_NUM,
                SiteKey.SIM_SAVE_DATABASE_NUM_DE);
        List<SubmitSimilarity> similarityList = new ArrayList<>();
        for (int i = 0; i < simResultList.size() && i < saveMaxNum; i++) {
            SimResult simResult = simResultList.get(i);
            SubmitSimilarity similarity = new SubmitSimilarity();
            similarity.setSubmitId1(simResult.getSubmId1());
            similarity.setSubmitId2(simResult.getSubmId2());
            similarity.setExamId(examId);
            similarity.setSimilarity(simResult.getValue());
            similarityList.add(similarity);
            // diff(simResult.getSubmId1(), simResult.getSubmId2(), simResult.getValue());
        }
        int insertCount = submitSimilarityDao.insertBatch(similarityList);
        if (insertCount != 0) {
            apiResult.setStatusAndDesc(1, "成功");
        } else {
            apiResult.setStatusAndDesc(-5, "插入失败");
        }
        return apiResult;
    }

    private void diff(Integer submId1, Integer submId2, double value) {
        SubmitInfo submitInfo1 = submitInfoDao.selectByPrimaryKey(submId1);
        SubmitInfo submitInfo2 = submitInfoDao.selectByPrimaryKey(submId2);
        Double oldValue = SimHash.getSimilarity(submitInfo1.getSource(), submitInfo2.getSource());
        System.out.printf("%d %d %f %f\n", submId1, submId2, value, oldValue);
    }
}
