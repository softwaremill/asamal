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
public class LayoutDirective extends Directive {
    
    public final static String LAYOUT = "ASAMAL_LAYOUT";
    
    @Override
    public String getName() {
        return "layout";
    }

    @Override
    public int getType() {
        return LINE;
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        Object value = node.jjtGetChild(0).value( context );
        String layoutName;
        if ( value != null )
        {
            layoutName = value.toString();
        }
        else
        {
            throw new ParseErrorException("The layout has to specify a name");
        }

        context.put(LAYOUT, layoutName);

        return true;
    }
}
