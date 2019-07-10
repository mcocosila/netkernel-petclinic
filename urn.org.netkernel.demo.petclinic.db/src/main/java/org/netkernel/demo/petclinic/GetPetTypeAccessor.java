package org.netkernel.demo.petclinic;

import org.netkernel.layer0.nkf.INKFRequest;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.representation.impl.HDSBuilder;
import org.netkernel.layer0.representation.impl.HDSNodeImpl;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

public class GetPetTypeAccessor extends StandardAccessorImpl
{

    public void onSource(INKFRequestContext context) throws Exception {

        String typeId = context.getThisRequest().getArgumentValue("typeId");

        // use freemarker to get the sql query
        INKFRequest request = context.createRequest("active:freemarker");
        request.addArgument("operator", "res:/resources/ftl/sql/select-pet-type.sql");
        request.addArgumentByValue("typeId", typeId);
        Object sqlQuery = context.issueRequest(request);

        // run the sql query and get an HDS response
        request = context.createRequest("active:sqlQuery");
        request.addArgumentByValue("operand", sqlQuery);
        request.setRepresentationClass(HDSNodeImpl.class);
        HDSNodeImpl petTypeHdsResp = (HDSNodeImpl)context.issueRequest(request);

        HDSBuilder builder = new HDSBuilder();
        builder.pushNode("type");
        builder.addNode("id", petTypeHdsResp.getFirstValue("resultset/row/id"));
        builder.addNode("name", petTypeHdsResp.getFirstValue("resultset/row/name"));

        context.createResponseFrom( builder.getRoot().getFirstNode("type") );
    }
}
