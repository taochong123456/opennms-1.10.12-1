package org.hyperic.hq.notifications.filtering;


import java.io.Serializable;
import java.util.Collection;
import java.util.Enumeration;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import org.hyperic.hq.measurement.shared.MeasurementManager;
import org.hyperic.hq.notifications.model.MetricNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("metricDestinationEvaluator")
public class MetricDestinationEvaluator extends DestinationEvaluator<MetricNotification> {
    @Override
    protected FilterChain<MetricNotification> instantiateFilterChain(Collection<Filter<MetricNotification,? extends FilteringCondition<?>>> filters) {
        return new FilterChain<MetricNotification>(filters);
    }
}

class DummyMsg implements ObjectMessage {
    Destination d = null;
    Serializable obj = null;
    
    public void acknowledge() throws JMSException {
    }

    public void clearBody() throws JMSException {
    }

    public void clearProperties() throws JMSException {
    }

    public boolean getBooleanProperty(String arg0) throws JMSException {
        return false;
    }

    public byte getByteProperty(String arg0) throws JMSException {
        return 0;
    }

    public double getDoubleProperty(String arg0) throws JMSException {
        return 0;
    }

    public float getFloatProperty(String arg0) throws JMSException {
        return 0;
    }

    public int getIntProperty(String arg0) throws JMSException {
        return 0;
    }

    public String getJMSCorrelationID() throws JMSException {
        return null;
    }

    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
        return null;
    }

    public int getJMSDeliveryMode() throws JMSException {
        return 0;
    }

    public Destination getJMSDestination() throws JMSException {
        return d;
    }

    public long getJMSExpiration() throws JMSException {
        return 0;
    }

    public String getJMSMessageID() throws JMSException {
        return null;
    }

    public int getJMSPriority() throws JMSException {
        return 0;
    }

    public boolean getJMSRedelivered() throws JMSException {
        return false;
    }

    public Destination getJMSReplyTo() throws JMSException {
        return null;
    }

    public long getJMSTimestamp() throws JMSException {
        return 0;
    }

    public String getJMSType() throws JMSException {
        return null;
    }

    public long getLongProperty(String arg0) throws JMSException {
        return 0;
    }

    public Object getObjectProperty(String arg0) throws JMSException {
        return null;
    }

    public Enumeration getPropertyNames() throws JMSException {
        return null;
    }

    public short getShortProperty(String arg0) throws JMSException {
        return 0;
    }

    public String getStringProperty(String arg0) throws JMSException {
        return null;
    }

    public boolean propertyExists(String arg0) throws JMSException {
        return false;
    }

    public void setBooleanProperty(String arg0, boolean arg1) throws JMSException {
    }

    public void setByteProperty(String arg0, byte arg1) throws JMSException {
    }

    public void setDoubleProperty(String arg0, double arg1) throws JMSException {
    }

    public void setFloatProperty(String arg0, float arg1) throws JMSException {
    }

    public void setIntProperty(String arg0, int arg1) throws JMSException {
    }

    public void setJMSCorrelationID(String arg0) throws JMSException {
    }

    public void setJMSCorrelationIDAsBytes(byte[] arg0) throws JMSException {
    }

    public void setJMSDeliveryMode(int arg0) throws JMSException {
    }

    public void setJMSDestination(Destination arg0) throws JMSException {
        this.d= arg0;
    }

    public void setJMSExpiration(long arg0) throws JMSException {
    }

    public void setJMSMessageID(String arg0) throws JMSException {
    }

    public void setJMSPriority(int arg0) throws JMSException {
    }

    public void setJMSRedelivered(boolean arg0) throws JMSException {
    }

    public void setJMSReplyTo(Destination arg0) throws JMSException {
    }

    public void setJMSTimestamp(long arg0) throws JMSException {
    }

    public void setJMSType(String arg0) throws JMSException {
    }

    public void setLongProperty(String arg0, long arg1) throws JMSException {
    }

    public void setObjectProperty(String arg0, Object arg1) throws JMSException {
    }

    public void setShortProperty(String arg0, short arg1) throws JMSException {
    }

    public void setStringProperty(String arg0, String arg1) throws JMSException {
    }

    public Serializable getObject() throws JMSException {
        return obj;
    }

    public void setObject(Serializable arg0) throws JMSException {
        this.obj=arg0;
    }
    
}