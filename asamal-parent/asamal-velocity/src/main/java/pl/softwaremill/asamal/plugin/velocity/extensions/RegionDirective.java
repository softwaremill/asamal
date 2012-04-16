package pl.softwaremill.asamal.plugin.velocity.extensions;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

/**
 * User: szimano
 */
public class RegionDirective extends Directive {
    
    public static final String REGION = "ASAMAL_REGION_";

    @Override
    public String getName() {
        return "region";
    }

    @Override
    public int getType() {
        return BLOCK;
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException,
            ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        Object value = node.jjtGetChild(0).value( context );
        String regionName;
        if ( value != null )
        {
            regionName = value.toString();
        }
        else
        {
            throw new ParseErrorException("The region has to specify a name");
        }

        context.put(REGION+regionName, node.jjtGetChild(1).literal());

        return true;
    }
}
