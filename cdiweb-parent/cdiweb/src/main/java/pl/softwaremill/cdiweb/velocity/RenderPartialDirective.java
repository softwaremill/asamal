package pl.softwaremill.cdiweb.velocity;

import org.apache.velocity.context.EvaluateContext;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.directive.StopCommand;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.util.introspection.Info;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.resource.ResourceResolver;

import java.io.IOException;
import java.io.StringReader;
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

        return ((ResourceResolver) context.get("resourceResolver")).resolvePartial(
                ((ControllerBean) context.get("controller")).getName(), partialName);
    }
}
