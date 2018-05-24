package org.opennms.web.rest.support;

import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;

public class JrobinFetchStrategyProvider implements MeasurementFetchStrategyProvider {
    @Override
    public Class<? extends MeasurementFetchStrategy> getStrategyClass(String timeSeriesStrategyName, String rrdStrategyClass) {
        if(!TimeSeries.RRD_TIME_SERIES_STRATEGY_NAME.equalsIgnoreCase(timeSeriesStrategyName) ||
                !JRobinRrdStrategy.class.getCanonicalName().equals(rrdStrategyClass)) {
            return null;
        }
        return JrobinFetchStrategy.class;
    }
}
