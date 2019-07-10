package org.netkernel.demo.petclinic;

import org.netkernel.layer0.nkf.INKFRequest;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.layer0.representation.IHDSNodeList;
import org.netkernel.layer0.representation.impl.HDSBuilder;
import org.netkernel.layer0.representation.impl.HDSNodeImpl;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

import java.util.Iterator;

public class GetPetForOwnerAccessor extends StandardAccessorImpl
{

    public void onSource(INKFRequestContext context) throws Exception {

        String ownerId = context.getThisRequest().getArgumentValue("ownerId");

        // use freemarker to get the sql query
        INKFRequest request = context.createRequest("active:freemarker");
        request.addArgument("operator", "res:/resources/ftl/sql/select-pets-by-ownerId.sql");
        request.addArgumentByValue("ownerId", ownerId);
        Object sqlQuery = context.issueRequest(request);

        // run the sql query and get an HDS response
        request = context.createRequest("active:sqlQuery");
        request.addArgumentByValue("operand", sqlQuery);
        request.setRepresentationClass(HDSNodeImpl.class);
        HDSNodeImpl petsHdsResp = (HDSNodeImpl)context.issueRequest(request);

        HDSBuilder builder = new HDSBuilder();
        builder.pushNode("pets");

        IHDSNodeList petsNodeList =  petsHdsResp.getNodes("resultset/row");
        Iterator<IHDSNode> itPets = petsNodeList.iterator();
        while(itPets.hasNext()) {
            IHDSNode petHds = itPets.next();

            // get the pet type
            Object typeId = petHds.getFirstValue("type_id");
            request = context.createRequest("res:/petclinic/api/pet-type/"+typeId);
            request.setRepresentationClass(HDSNodeImpl.class);
            HDSNodeImpl petTypeHdsResp = (HDSNodeImpl)context.issueRequest(request);

            // get visits
            Object id = petHds.getFirstValue("id");
            request = context.createRequest("res:/petclinic/api/pet-visit/"+id);
            request.setRepresentationClass(HDSNodeImpl.class);
            HDSNodeImpl visitResp = (HDSNodeImpl)context.issueRequest(request);

            // build the hds pet node
            builder.pushNode("pet");
            builder.addNode("id", petHds.getFirstValue("id"));
            builder.addNode("name", petHds.getFirstValue("name"));
            builder.addNode("birthDate", petHds.getFirstValue("birth_date"));

            builder.pushNode("type");
            builder.addNode("id", petTypeHdsResp.getFirstValue("id"));
            builder.addNode("name", petTypeHdsResp.getFirstValue("name"));
            builder.popNode();

            builder.addNode("owner", ownerId);

            builder.pushNode("visits");
            IHDSNodeList visitNodeList =  visitResp.getNodes("visit");
            Iterator<IHDSNode> itVisits = visitNodeList.iterator();
            while(itVisits.hasNext()) {
                IHDSNode visitHds = itVisits.next();
                builder.pushNode("visit");
                builder.addNode("id", visitHds.getFirstValue("id"));
                builder.addNode("pet", visitHds.getFirstValue("pet"));
                builder.addNode("visitDate", visitHds.getFirstValue("visitDate"));
                builder.addNode("description", visitHds.getFirstValue("description"));
                builder.popNode();
            }
            builder.popNode();  // pop visits

            builder.popNode();  // pop pet
        }

        context.createResponseFrom(builder.getRoot());
    }
}
