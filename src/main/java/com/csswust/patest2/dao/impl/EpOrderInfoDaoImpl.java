package com.csswust.patest2.dao.impl;

import com.csswust.patest2.dao.common.BaseQuery;
import com.csswust.patest2.dao.common.CommonMapper;
import com.csswust.patest2.dao.EpOrderInfoDao;
import com.csswust.patest2.entity.EpOrderInfo;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public class EpOrderInfoDaoImpl extends CommonMapper<EpOrderInfo, BaseQuery> implements EpOrderInfoDao {
    @Override
    public String getPackage() {
        return "com.csswust.patest2.dao.EpOrderInfoDao.";
    }

    @Override
    public void insertInit(EpOrderInfo record, Date date) {
        record.setOrderId(null);
        record.setCreateTime(date);
        record.setModifyTime(date);
        record.setModifyUserId(getUserId());
    }

    @Override
    public void updateInit(EpOrderInfo record, Date date) {
        record.setModifyTime(date);
        record.setModifyUserId(getUserId());
    }
}
