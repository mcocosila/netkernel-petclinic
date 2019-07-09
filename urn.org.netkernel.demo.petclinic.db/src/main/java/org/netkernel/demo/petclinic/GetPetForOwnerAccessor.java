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

        IHDSNodeList petsNodeList =  petsHdsResp.getNodes("resultset/row");
        ArrayList<JSONObject> petJsonList = new ArrayList<>();
        Iterator<IHDSNode> it = petsNodeList.iterator();
        while(it.hasNext()) {
            IHDSNode petHds = it.next();
            JSONObject petJson = new JSONObject();

            Object id = petHds.getFirstValue("id");
            Object name = petHds.getFirstValue("name");
            Object birthDate = petHds.getFirstValue("birth_date");
            Object typeId = petHds.getFirstValue("type_id");
            //Object ownerId = petHds.getFirstValue("owner_id");

            // get the pet type
            request = context.createRequest("res:/petclinic/api/java/pet-type/"+typeId);
            request.setRepresentationClass(JSONObject.class);
            JSONObject typeJsonResp = (JSONObject)context.issueRequest(request);

            // get visits
            request = context.createRequest("res:/petclinic/api/java/pet-visit/"+id);
            Object visitResp = context.issueRequest(request);

            petJson.put("id", id);
            petJson.put("name", name);
            petJson.put("birthDate", birthDate);
            petJson.put("type", typeJsonResp);
            petJson.put("owner", ownerId);
            petJson.put("visits", visitResp);

            petJsonList.add(petJson);
        }

        INKFResponse response = context.createResponseFrom(petJsonList);
    }
}
