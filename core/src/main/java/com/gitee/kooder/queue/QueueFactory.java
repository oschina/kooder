/**
 * Copyright (c) 2021, OSChina (oschina.net@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitee.kooder.queue;

import com.gitee.kooder.core.KooderConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

/**
 * 队列工厂
 * @author Winter Lau<javayou@gmail.com>
 */
public class QueueFactory {

    static QueueProvider provider;

    static {
        Properties props = KooderConfig.getQueueProperties();
        String type = StringUtils.trim(props.getProperty("provider"));
        if("redis".equalsIgnoreCase(type))
            provider = new RedisQueueProvider(props);
        else if("embed".equalsIgnoreCase(type))
            provider = new EmbedQueueProvider(props);
    }

    public final static QueueProvider getProvider() {
        return provider;
    }

}
