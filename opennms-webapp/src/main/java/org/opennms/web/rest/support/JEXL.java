package org.opennms.web.rest.support;

import java.util.Map;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

import com.google.common.collect.Maps;
import com.google.common.collect.RowSortedTable;

/**
 * Generic JEXL expression filter
 *
 * @author jwhite
 */
@FilterInfo(name="JEXL", description="Generic JEXL expression filter")
public class JEXL implements Filter {

    @FilterParam(key="expression", required=true, displayName="Expression", description="JEXL expression.")
    private String m_expression;

    private final JexlEngine jexl;

    protected JEXL() {
        jexl = new JexlEngine();
        // Add additional functions to the engine
        Map<String, Object> functions = Maps.newHashMap();
        functions.put("math", Math.class);
        functions.put("strictmath", StrictMath.class);
        jexl.setFunctions(functions);
    }

    public JEXL(String expression) {
        this();
        m_expression = expression;
    }

    @Override
    public void filter(RowSortedTable<Long, String, Double> qrAsTable)
            throws Exception {

        // Prepare the JEXL context
        final Map<String, Object> jexlValues = Maps.newHashMap();
        jexlValues.put("table", qrAsTable);
        final JexlContext context = new MapContext(jexlValues);

        // Compile the expression
        Expression expression = jexl.createExpression(m_expression);

        // Evaluate the expression
        expression.evaluate(context);
    }
}
