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

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

/**
 * Abstract class used by velocity evaluators

 * User: szimano
 */
public abstract class AbstractVelocityEvaluator extends Directive {

    public abstract String getStringToEvaluate(InternalContextAdapter context, Writer writer, Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException;

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException,
            ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        /*
         * The new string needs to be parsed since the text has been dynamically generated.
         */
        String templateName = context.getCurrentTemplateName();
        SimpleNode nodeTree = null;

        try
        {
            String regionContent = getStringToEvaluate(context, writer, node);

            if (regionContent != null) {
                nodeTree = rsvc.parse(new StringReader(regionContent),
                        templateName, false);
            }
            else {
                // no content provided, render nothing
                return true;
            }
        }
        catch (ParseException pex)
        {
            // use the line/column from the template
            Info info = new Info( templateName, node.getLine(), node.getColumn() );
            throw  new ParseErrorException( pex.getMessage(), info );
        }
        catch (TemplateInitException pex)
        {
            Info info = new Info( templateName, node.getLine(), node.getColumn() );
            throw  new ParseErrorException( pex.getMessage(), info );
        }

        /*
         * now we want to init and render.  Chain the context
         * to prevent any changes to the current context.
         */

        if (nodeTree != null)
        {
            InternalContextAdapter ica = new EvaluateContext(context, rsvc);

            ica.pushCurrentTemplateName( templateName );

            try
            {
                try
                {
                    nodeTree.init( ica, rsvc );
                }
                catch (TemplateInitException pex)
                {
                    Info info = new Info( templateName, node.getLine(), node.getColumn() );
                    throw  new ParseErrorException( pex.getMessage(), info );
                }

                try
                {
                    preRender(ica);

                    /*
                     *  now render, and let any exceptions fly
                     */
                    nodeTree.render( ica, writer );
                }
                catch (StopCommand stop)
                {
                    if (!stop.isFor(this))
                    {
                        throw stop;
                    }
                    else if (rsvc.getLog().isDebugEnabled())
                    {
                        rsvc.getLog().debug(stop.getMessage());
                    }
                }
                catch (ParseErrorException pex)
                {
                    // convert any parsing errors to the correct line/col
                    Info info = new Info( templateName, node.getLine(), node.getColumn() );
                    throw  new ParseErrorException( pex.getMessage(), info );
                }
            }
            finally
            {
                ica.popCurrentTemplateName();
                postRender(ica);
            }
            return true;
        }

        return false;
    }
}
