package org.netkernel.demo.petclinic;

import org.json.JSONObject;
import org.netkernel.layer0.nkf.INKFRequest;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.layer0.representation.IHDSNodeList;
import org.netkernel.layer0.representation.impl.HDSNodeImpl;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

public class GetOwnerAccessor extends StandardAccessorImpl
{

    public void onSource(INKFRequestContext context) throws Exception {

        String ownerId = context.getThisRequest().getArgumentValue("ownerId");

        // use freemarker to get the sql query
        INKFRequest request = context.createRequest("active:freemarker");
        request.addArgument("operator", "res:/resources/ftl/sql/select-ownerId.sql");
        request.addArgumentByValue("ownerId", ownerId);
        Object sqlQuery = context.issueRequest(request);

        // run the sql query and get an HDS response
        request = context.createRequest("active:sqlQuery");
        request.addArgumentByValue("operand", sqlQuery);
        Object hdsRep = context.issueRequest(request);

        // convert hds to json
        request = context.createRequest("active:JSONFromHDS");
        request.addArgumentByValue("operand", hdsRep);
        request.setRepresentationClass(JSONObject.class);
        JSONObject jsonResp = (JSONObject)context.issueRequest(request);

        JSONObject ownerJson = jsonResp.getJSONObject("resultset").getJSONObject("row");

        // Get the pets
        request = context.createRequest("res:/petclinic/api/java/pets-for-owner/"+ownerId);
        //request.setRepresentationClass(JSONObject.class);
        Object petsJsonResp = context.issueRequest(request);
        ownerJson.put("pets", petsJsonResp);

        INKFResponse response = context.createResponseFrom(ownerJson);
    }
}
