package pl.softwaremill.asamal.plugin.velocity.extensions;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;
import pl.softwaremill.asamal.extension.view.ContextConstants;
import pl.softwaremill.asamal.extension.view.ResourceResolver;

import java.io.IOException;
import java.io.Writer;

/**
 * User: szimano
 */
public class RenderPartialDirective extends AbstractVelocityEvaluator {
    @Override
    public String getName() {
        return "renderPartial";
    }

    @Override
    public int getType() {
        return LINE;
    }

    @Override
    public String getStringToEvaluate(InternalContextAdapter context, Writer writer, Node node) throws IOException,
            ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        /*
        * Evaluate the string with the current context.  We know there is
        * exactly one argument and it is a string or reference.
        */

        Object value = node.jjtGetChild(0).value( context );
        String partialName;
        if ( value != null )
        {
            partialName = value.toString();
        }
        else
        {
            throw new ParseErrorException("The renderPartial has to specify a name");
        }

        try {
            return ((ResourceResolver) context.get("resourceResolver")).resolvePartial(
                    context.get(ContextConstants.CONTROLLER_NAME).toString(), partialName, ".vm");
        } catch (pl.softwaremill.asamal.extension.exception.ResourceNotFoundException e) {
            throw new RuntimeException("Cannot find partial " + partialName, e);
        }
    }
}
