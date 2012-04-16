package pl.softwaremill.asamal.plugin.velocity.extensions;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.Writer;

/**
 * User: szimano
 */
public class IncludeRegionDirective extends AbstractVelocityEvaluator {
    @Override
    public String getName() {
        return "includeRegion";
    }

    @Override
    public int getType() {
        return LINE;
    }

    @Override
    public String getStringToEvaluate(InternalContextAdapter context, Writer writer, Node node) {
        /*
        * Evaluate the string with the current context.  We know there is
        * exactly one argument and it is a string or reference.
        */

        Object value = node.jjtGetChild(0).value( context );
        String regionName;
        if ( value != null )
        {
            regionName = value.toString();
        }
        else
        {
            throw new ParseErrorException("The includeRegion has to specify a name");
        }

        return (String) context.get(RegionDirective.REGION + regionName);
    }
}
