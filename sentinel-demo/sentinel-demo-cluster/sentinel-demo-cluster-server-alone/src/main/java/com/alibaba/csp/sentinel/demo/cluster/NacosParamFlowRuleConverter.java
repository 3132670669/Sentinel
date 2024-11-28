package com.alibaba.csp.sentinel.demo.cluster;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 文件描述
 *
 * @author keep simple
 * @since 2024/11/25
 */
public class NacosParamFlowRuleConverter implements Converter<String, Collection<ParamFlowRule>> {
	
	@Override
	public List<ParamFlowRule> convert(String s) {
		List<ParamFlowRuleEntity> ruleEntities = JSON.parseArray(s, ParamFlowRuleEntity.class);
		
		return CollectionUtils.isEmpty(ruleEntities)
				       ? Collections.emptyList()
				       : ruleEntities.stream()
						         .map(ParamFlowRuleEntity::toRule)
						         .toList();
	}
}
