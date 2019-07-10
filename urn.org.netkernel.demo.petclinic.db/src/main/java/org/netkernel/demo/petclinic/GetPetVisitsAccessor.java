package org.netkernel.demo.petclinic;

import org.netkernel.layer0.nkf.INKFRequest;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.layer0.representation.IHDSNodeList;
import org.netkernel.layer0.representation.impl.HDSBuilder;
import org.netkernel.layer0.representation.impl.HDSNodeImpl;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

import java.util.Iterator;

public class GetPetVisitsAccessor extends StandardAccessorImpl
{

    public void onSource(INKFRequestContext context) throws Exception {

        String petId = context.getThisRequest().getArgumentValue("petId");

        // use freemarker to get the sql query
        INKFRequest request = context.createRequest("active:freemarker");
        request.addArgument("operator", "res:/resources/ftl/sql/select-pet-visits.sql");
        request.addArgumentByValue("petId", petId);
        Object sqlQuery = context.issueRequest(request);

        // run the sql query and get an HDS response
        request = context.createRequest("active:sqlQuery");
        request.addArgumentByValue("operand", sqlQuery);
        request.setRepresentationClass(HDSNodeImpl.class);
        HDSNodeImpl petVisitsHdsResp = (HDSNodeImpl)context.issueRequest(request);


        IHDSNodeList visitNodeList =  petVisitsHdsResp.getNodes("resultset/row");
        HDSBuilder builder = new HDSBuilder();
        Iterator<IHDSNode> it = visitNodeList.iterator();
        while(it.hasNext()) {
            IHDSNode visitHds = it.next();

            builder.pushNode("visit");
            builder.addNode("id", visitHds.getFirstValue("id"));
            builder.addNode("pet", visitHds.getFirstValue("pet_id"));
            builder.addNode("visitDate", visitHds.getFirstValue("visit_date"));
            builder.addNode("description", visitHds.getFirstValue("description"));
            builder.popNode();

        }

        context.createResponseFrom( builder.getRoot() );
    }
}
