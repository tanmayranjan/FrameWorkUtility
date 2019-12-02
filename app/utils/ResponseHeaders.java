package utils;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import java.util.concurrent.CompletionStage;
public class ResponseHeaders extends Action.Simple {
    @Override
    public CompletionStage<Result>  call(final Http.Context ctx) {
        ctx.response().setHeader("Access-Control-Allow-Origin","*");
        ctx.response().setHeader("Access-Control-Allow-Methods", "*");
        ctx.response().setHeader("Access-Control-Allow-Headers", "*");
        ctx.response().setHeader("Access-Control-Allow-Credentials", "true");
        return delegate.call(ctx);
    }
}