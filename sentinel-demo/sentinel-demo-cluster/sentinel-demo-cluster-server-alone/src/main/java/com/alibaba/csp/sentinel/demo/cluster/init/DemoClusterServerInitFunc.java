/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.cluster.init;

import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.demo.cluster.DemoConstants;
import com.alibaba.csp.sentinel.demo.cluster.NacosParamFlowRuleConverter;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.PropertyKeyConst;

import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author Eric Zhao
 */
public class DemoClusterServerInitFunc implements InitFunc {
    
    private final String remoteAddress = "127.0.0.1:8848";
    private final String groupId = "SENTINEL_GROUP";
    private final String namespaceSetDataId = "cluster-server-namespace-set";
    private final String serverTransportDataId = "cluster-server-transport-config";

    @Override
    public void init() throws Exception {
        
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, remoteAddress);
        properties.put(PropertyKeyConst.USERNAME, "nacos");
        properties.put(PropertyKeyConst.PASSWORD, "nacos");
        properties.put(PropertyKeyConst.NAMESPACE, "955419e3-2203-4ef6-9443-fc2758ddc6bc");
        
        // Register cluster flow rule property supplier which creates data source by namespace.
        ClusterFlowRuleManager.setPropertySupplier(namespace -> {
            ReadableDataSource<String, List<FlowRule>> ds = new NacosDataSource<>(properties,
                                                                                  groupId,
                namespace + DemoConstants.FLOW_POSTFIX,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
            return ds.getProperty();
        });
        // Register cluster parameter flow rule property supplier.
        ClusterParamFlowRuleManager.setPropertySupplier(namespace -> {
            ReadableDataSource<String, List<ParamFlowRule>> ds = new NacosDataSource<>(
                    properties,
                    groupId,
                    namespace + DemoConstants.PARAM_FLOW_POSTFIX,
//                source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {}));
                    source -> {
                        NacosParamFlowRuleConverter converter = new NacosParamFlowRuleConverter();
                        return converter.convert(source);
                    }
            );
            return ds.getProperty();
        });

        // Server namespace set (scope) data source.
        ReadableDataSource<String, Set<String>> namespaceDs = new NacosDataSource<>(properties,
                                                                                    groupId,
            namespaceSetDataId, source -> JSON.parseObject(source, new TypeReference<Set<String>>() {}));
        ClusterServerConfigManager.registerNamespaceSetProperty(namespaceDs.getProperty());
        // Server transport configuration data source.
        ReadableDataSource<String, ServerTransportConfig> transportConfigDs = new NacosDataSource<>(
                properties,
            groupId, serverTransportDataId,
            source -> JSON.parseObject(source, new TypeReference<ServerTransportConfig>() {}));
        ClusterServerConfigManager.registerServerTransportProperty(transportConfigDs.getProperty());
    }
}
