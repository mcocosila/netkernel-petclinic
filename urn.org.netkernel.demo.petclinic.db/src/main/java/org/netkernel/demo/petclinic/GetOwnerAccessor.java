package org.netkernel.demo.petclinic;

import org.json.JSONObject;
import org.netkernel.layer0.nkf.INKFRequest;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.layer0.representation.IHDSNodeList;
import org.netkernel.layer0.representation.impl.HDSBuilder;
import org.netkernel.layer0.representation.impl.HDSNodeImpl;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        request.setRepresentationClass(HDSNodeImpl.class);
        HDSNodeImpl hdsOwnerResp = (HDSNodeImpl)context.issueRequest(request);

        // Get the pets
        request = context.createRequest("res:/petclinic/api/pets-for-owner/"+ownerId);
        request.setRepresentationClass(HDSNodeImpl.class);
        HDSNodeImpl petsHdsResp = (HDSNodeImpl)context.issueRequest(request);

        IHDSNode ownerHds = buildOwnerHds(hdsOwnerResp, petsHdsResp);

//        // convert hds to json
//        request = context.createRequest("active:JSONFromHDS");
//        request.addArgumentByValue("operand", ownerHds);
//        request.setRepresentationClass(JSONObject.class);
//        JSONObject jsonResp = (JSONObject)context.issueRequest(request);

        JSONObject ownerJson = hdsToJson(ownerHds);
        INKFResponse response = context.createResponseFrom(ownerJson);
        response.setMimeType("application/json;charset=UTF-8");
    }


    /**
     * Builds an HS representation of the owner
     *
     * @param hdsOwnerResp
     * @param petsHdsResp
     * @return
     */
    private IHDSNode buildOwnerHds(HDSNodeImpl hdsOwnerResp, HDSNodeImpl petsHdsResp) {
        HDSBuilder builder = new HDSBuilder();

        builder.addNode("id", hdsOwnerResp.getFirstValue("resultset/row/id"));
        builder.addNode("firstName", hdsOwnerResp.getFirstValue("resultset/row/first_name"));
        builder.addNode("lastName", hdsOwnerResp.getFirstValue("resultset/row/last_name"));
        builder.addNode("address", hdsOwnerResp.getFirstValue("resultset/row/address"));
        builder.addNode("city", hdsOwnerResp.getFirstValue("resultset/row/city"));
        builder.addNode("telephone", hdsOwnerResp.getFirstValue("resultset/row/telephone"));

        //builder.pushNode("pets");
        IHDSNodeList petsNodeList =  petsHdsResp.getNodes("pets/pet");
        Iterator<IHDSNode> itPets = petsNodeList.iterator();
        while(itPets.hasNext()) {
            IHDSNode petHds = itPets.next();

            builder.pushNode("pet");
            builder.addNode("id", petHds.getFirstValue("id"));
            builder.addNode("name", petHds.getFirstValue("name"));
            builder.addNode("birthDate", petHds.getFirstValue("birthDate"));

            builder.pushNode("type");
            builder.addNode("id", petHds.getFirstValue("type/id"));
            builder.addNode("name", petHds.getFirstValue("type/name"));
            builder.popNode();

            builder.addNode("owner", petHds.getFirstValue("owner"));

            IHDSNodeList visitNodeList =  petHds.getNodes("visits/visit");
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

            builder.popNode();  // pop pet
        }

        return builder.getRoot();
    }

    /**
     * This should be replaced by a transreptor
     *
     * @param ownerHds
     * @return
     */
    private JSONObject hdsToJson(IHDSNode ownerHds) {
        JSONObject ownerJson = new JSONObject();

        ownerJson.put("id", ownerHds.getFirstValue("id"));
        ownerJson.put("firstName", ownerHds.getFirstValue("firstName"));
        ownerJson.put("lastName", ownerHds.getFirstValue("lastName"));
        ownerJson.put("address", ownerHds.getFirstValue("address"));
        ownerJson.put("city", ownerHds.getFirstValue("city"));
        ownerJson.put("telephone", ownerHds.getFirstValue("telephone"));

        List<JSONObject> petsJsonList = new ArrayList<>();
        IHDSNodeList petsNodeList =  ownerHds.getNodes("pet");
        Iterator<IHDSNode> itPets = petsNodeList.iterator();
        while(itPets.hasNext()) {
            IHDSNode petHds = itPets.next();
            JSONObject petJson = new JSONObject();
            petJson.put("id", petHds.getFirstValue("id"));
            petJson.put("name", petHds.getFirstValue("name"));
            petJson.put("birthDate", petHds.getFirstValue("birthDate"));

            JSONObject type = new JSONObject();
            type.put("id", petHds.getFirstValue("type/id"));
            type.put("name", petHds.getFirstValue("type/name"));
            petJson.put("type", type);

            petJson.put("owner", petHds.getFirstValue("owner"));

            List<JSONObject> visitJsonList = new ArrayList<>();
            IHDSNodeList visitNodeList =  petHds.getNodes("visit");
            Iterator<IHDSNode> itVisits = visitNodeList.iterator();
            while(itVisits.hasNext()) {
                IHDSNode visitHds = itVisits.next();
                JSONObject visitJson = new JSONObject();
                visitJson.put("id", visitHds.getFirstValue("id"));
                visitJson.put("pet", visitHds.getFirstValue("pet"));
                visitJson.put("visitDate", visitHds.getFirstValue("visitDate"));
                visitJson.put("description", visitHds.getFirstValue("description"));
                visitJsonList.add(visitJson);
            }
            petJson.put("visits", visitJsonList);

            petsJsonList.add(petJson);
        }
        ownerJson.put("pets", petsJsonList);

        return ownerJson;
    }
}
