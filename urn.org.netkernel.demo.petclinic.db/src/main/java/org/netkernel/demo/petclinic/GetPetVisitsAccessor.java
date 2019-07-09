package org.netkernel.demo.petclinic;

import org.json.JSONObject;
import org.netkernel.layer0.nkf.INKFRequest;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.layer0.representation.IHDSNodeList;
import org.netkernel.layer0.representation.impl.HDSNodeImpl;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

import java.util.ArrayList;
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
        ArrayList<JSONObject> visitJsonList = new ArrayList<>();
        Iterator<IHDSNode> it = visitNodeList.iterator();
        while(it.hasNext()) {
            IHDSNode visitHds = it.next();

            // convert hds to json
            request = context.createRequest("active:JSONFromHDS");
            request.addArgumentByValue("operand", visitHds);
            request.setRepresentationClass(JSONObject.class);
            JSONObject jsonResp = (JSONObject)context.issueRequest(request);

            jsonResp.remove("pet_id");
            jsonResp.put("pet", petId);

            visitJsonList.add(jsonResp);
        }

        INKFResponse response = context.createResponseFrom( visitJsonList );
    }
}
