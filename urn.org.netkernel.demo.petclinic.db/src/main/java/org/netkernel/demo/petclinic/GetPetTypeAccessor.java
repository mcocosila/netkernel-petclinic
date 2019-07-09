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
        HDSNodeImpl petsTypeHdsResp = (HDSNodeImpl)context.issueRequest(request);


        // convert hds to json
        request = context.createRequest("active:JSONFromHDS");
        request.addArgumentByValue("operand", petsTypeHdsResp);
        request.setRepresentationClass(JSONObject.class);
        JSONObject jsonResp = (JSONObject)context.issueRequest(request);

        JSONObject petTypeJson = jsonResp.getJSONObject("resultset").getJSONObject("row");

        INKFResponse response = context.createResponseFrom( petTypeJson );
    }
}
