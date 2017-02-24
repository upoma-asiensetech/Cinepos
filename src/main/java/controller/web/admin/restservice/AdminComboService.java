package controller.web.admin.restservice;

import controller.web.admin.AdminUriPreFix;
import dao.*;
import entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import utility.ServiceResponse;
import validator.admin.AdminComboService.createCombo.*;
import validator.admin.AdminDistributorService.editDistributor.editDistributorForm;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Sarwar on 1/19/2017.
 */
@RestController
@RequestMapping(AdminUriPreFix.apiUriPrefix +"/combo")
public class AdminComboService {
   @Autowired
    ComboDao comboDao;

   @Autowired
   CreateComboValidator createComboValidator;

   @Autowired
   ComboProductValidator comboProductValidator;

   @Autowired
   ConcessionProductDao concessionProductDao;

   @Autowired
   ComboDetailDao comboDetailDao;

   @Autowired
   SeatTypeDao seatTypeDao;

   @Autowired
   SellDetailsDao sellDetailsDao;

    private final static String COMBO_TICKET="TICKET";
    private final static String COMBO_PRODUCT="PRODUCT";

    private final static String COMBO_TYP_TICKET ="TICKET_PRODUCT";
    private final static String COMBO_TYP_PRODUCT ="PRODUCT";

    @RequestMapping(value = "/create",method = RequestMethod.POST)
    public ResponseEntity<?> create(Authentication authentication,
                                    @Valid CreateComboForm createComboForm,
                                    BindingResult result,
                                    HttpServletRequest request){
        AuthCredential currentLoggedInUser = (AuthCredential)authentication.getPrincipal();
        String errorMsg="Combo create successfully";

        try {
            ServiceResponse serviceResponse = ServiceResponse.getInstance();
            serviceResponse.bindValidationError(result);

            /**
             * Basic validation STARTS
             * */

            if (serviceResponse.hasErrors()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(serviceResponse.getFormError());
            }

            createComboValidator.validate(createComboForm,result);
            serviceResponse.bindValidationError(result);

            if (serviceResponse.hasErrors()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(serviceResponse.getFormError());
            }
            /**
             * Basic validation ENDS
             * */


            /**
             * Business validation validation ENDS
             * */
            String tmpComboType=null;
            switch(createComboForm.getComboType()){
                case "product":
                    tmpComboType = COMBO_TYP_TICKET;
                    break;
                case "ticket":
                    tmpComboType = COMBO_TYP_PRODUCT;
                    break;
                default:
                    serviceResponse.setValidationError("comboType","Unknown combo type : "+createComboForm.getComboType());
                    break;
            }
            if(serviceResponse.hasErrors()){
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(serviceResponse.getFormError());
            }

            Combo combo=new Combo();
            combo.setComboName(createComboForm.getComboName());
            combo.setDetails(createComboForm.getDetails());
            combo.setPrice(createComboForm.getPrice());
            combo.setStartDate(createComboForm.getFormattedStartDate());
            combo.setEndDate(createComboForm.getFormattedEndDate());
            combo.setComboType(tmpComboType);
            combo.setStatus(1);
            combo.setCreatedBy(currentLoggedInUser.getId());

            comboDao.insert(combo);

            List<ComboDetails> comboProductArray = new ArrayList<>();

            /**
             * Get Combo Product
             * */
            List<ComboProductDetailsForm> comboProductsListDetailsForm = createComboForm.getComboProductDetailsForm();


            for (ComboProductDetailsForm tgtComboProductDetailsForm : comboProductsListDetailsForm){


                ComboDetails comboDetail=new ComboDetails();
                comboDetail.setComboId(combo.getId());

                comboDetail.setProductQuantity(tgtComboProductDetailsForm.getQuantity());
                if(tgtComboProductDetailsForm.getProductId() == null){
                    serviceResponse.setValidationError("productId","Product ID required");
                    break;
                }

                if(tgtComboProductDetailsForm.getType().equals(COMBO_PRODUCT)){
                    comboDetail.setComboProductType(COMBO_PRODUCT);

                    ConcessionProduct concessionProduct = concessionProductDao.getById(tgtComboProductDetailsForm.getProductId());

                    if(concessionProduct==null){
                        serviceResponse.setValidationError("productId","No product found by ID:+"+tgtComboProductDetailsForm.getProductId());
                        break;
                    }

                    comboDetail.setConcessionProductId(concessionProduct.getId());
                    comboDetail.setProductQuantity(tgtComboProductDetailsForm.getQuantity());
                    comboDetail.setTicketQuantity(0);
                } else if (tgtComboProductDetailsForm.getType().equals(COMBO_TICKET)) {
                    comboDetail.setComboProductType(COMBO_TICKET);

                    SeatType seatType = seatTypeDao.getById(tgtComboProductDetailsForm.getProductId());

                    if(seatType==null){
                        serviceResponse.setValidationError("productId","No Seat Type found by ID:+"+tgtComboProductDetailsForm.getProductId());
                        break;
                    }

                    comboDetail.setSeatTypeId(seatType.getId());
                    comboDetail.setProductQuantity(0);
                    comboDetail.setTicketQuantity(1);
                }else{
                    serviceResponse.setValidationError("type","Type required");
                    break;
                }
                comboDetail.setCreatedBy(currentLoggedInUser.getId());
                comboProductArray.add(comboDetail);
            }

            if(serviceResponse.hasErrors()){
                if(combo!=null)comboDao.delete(combo);
                return ResponseEntity.status(HttpStatus.OK).body(ServiceResponse.getMsg(errorMsg));

            }
            combo.setComboDetails(comboProductArray);
            /**
             * Updating Combo
             * */
            comboDao.update(combo);



        }catch (Exception e){

        }

        return ResponseEntity.status(HttpStatus.OK).body(ServiceResponse.getMsg(errorMsg));



    }

    @RequestMapping(value = "/edit/{comboId}",method = RequestMethod.POST)
    public ResponseEntity<?> edit(  Authentication authentication,
                                    @Valid CreateComboForm createComboForm,
                                    BindingResult result,
                                    HttpServletRequest request,
                                    @PathVariable Integer comboId){
        AuthCredential currentLoggedInUser = (AuthCredential)authentication.getPrincipal();


        ServiceResponse serviceResponse = ServiceResponse.getInstance();
        serviceResponse.bindValidationError(result);
        Combo combo=comboDao.getById(comboId);


        if (combo==null) {
            serviceResponse.setValidationError("comboId","No Combo found with ID : "+comboId);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(serviceResponse.getFormError());
        }

        if (serviceResponse.hasErrors()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(serviceResponse.getFormError());
        }

        createComboValidator.validate(createComboForm,result);
        serviceResponse.bindValidationError(result);

        if (serviceResponse.hasErrors()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(serviceResponse.getFormError());
        }

        String tmpComboType=null;
        switch(createComboForm.getComboType()){
            case "product":
                tmpComboType = COMBO_TYP_PRODUCT;
                break;
            case "ticket":
                tmpComboType = COMBO_TYP_TICKET;
                break;
            default:
                serviceResponse.setValidationError("comboType","Unknown combo type : "+createComboForm.getComboType());
                break;
        }
        if(serviceResponse.hasErrors()){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(serviceResponse.getFormError());
        }

        combo.setComboName(createComboForm.getComboName());
        combo.setDetails(createComboForm.getDetails());
        combo.setPrice(createComboForm.getPrice());
        combo.setStartDate(createComboForm.getFormattedStartDate());
        combo.setEndDate(createComboForm.getFormattedEndDate());
        combo.setComboType(tmpComboType);


        /**
         *  Combo products
         *  */

        Set<ComboDetails> comboDetailArray = new HashSet<>();
        /**
         * Get Combo Product
         * */
        List<ComboProductDetailsForm> comboProductsListDetailsForm = createComboForm.getComboProductDetailsForm();

        for (ComboProductDetailsForm tgtComboProductDetailsForm : comboProductsListDetailsForm){
            ComboDetails comboDetails = null;
            Integer comboProductId = 0;
            String COMBO_PRODUCT_TYP = null;

            if(tgtComboProductDetailsForm.getType().equals(COMBO_PRODUCT)){
                /**
                 * Setting product type to PRODUCT
                * */
                COMBO_PRODUCT_TYP = COMBO_PRODUCT;

                ConcessionProduct concessionProduct = concessionProductDao.getById(tgtComboProductDetailsForm.getProductId());

                /**
                 * Product existence checking
                 * */
                if(concessionProduct==null){
                    serviceResponse.setValidationError("productId","No product found with ID : "+tgtComboProductDetailsForm.getProductId());
                    break;
                }
                /**
                 * Get combo details by product
                 * */
                comboDetails= comboDetailDao.getByComboIdAndProductId(comboId, tgtComboProductDetailsForm.getProductId());

            }else if(tgtComboProductDetailsForm.getType().equals(COMBO_TICKET)) {
                /**
                 * Setting product type to TICKET
                 * */
                COMBO_PRODUCT_TYP = COMBO_TICKET;

                SeatType seatType = seatTypeDao.getById(tgtComboProductDetailsForm.getProductId());

                /**
                 * Seat type existence checking
                 * */
                if(seatType==null){
                    serviceResponse.setValidationError("productId","No seat type found with ID : "+tgtComboProductDetailsForm.getProductId());
                    break;
                }

                /**
                 * Get combo details by seat id
                 * */
                comboDetails = comboDetailDao.getSeatTypeByComboIdAndSeatTypeId(comboId, seatType.getId());
            }else{
                serviceResponse.setValidationError("type","No seat type found with ID : "+tgtComboProductDetailsForm.getProductId());
                break;
            }

            comboProductId = tgtComboProductDetailsForm.getProductId();

            /**
             * If new seat type  or new product added is added
             * */

            if(comboDetails==null){
                comboDetails = new ComboDetails();
                comboDetails.setComboId(combo.getId());
                comboDetails.setCreatedBy(currentLoggedInUser.getId());
                comboDetails.setComboProductType(COMBO_PRODUCT_TYP);

            }

            if(COMBO_PRODUCT_TYP.equals(COMBO_PRODUCT)){
                comboDetails.setConcessionProductId(comboProductId);
                comboDetails.setProductQuantity(tgtComboProductDetailsForm.getQuantity());
                comboDetails.setTicketQuantity(0);
            }else if(COMBO_PRODUCT_TYP.equals(COMBO_TICKET)){
                comboDetails.setSeatTypeId(comboProductId);
                comboDetails.setProductQuantity(0);
                comboDetails.setTicketQuantity(tgtComboProductDetailsForm.getQuantity());
            }

            comboDetailArray.add(comboDetails);
        }
        /**
         * If  seat type  or product is not exist
         * error occurred in iteration for json form
         * */
        if(serviceResponse.hasErrors()){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(serviceResponse.getFormError());
        }



        boolean comboDetailsUpdateFlag = comboDetailDao.insertOrUpdateBatch(comboDetailArray);

        if(!comboDetailsUpdateFlag){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ServiceResponse.getMsg("Internal server error while updating Combo"));
        }
        combo.setComboDetails(new ArrayList<>(comboDetailArray));
        /**
         * Updating Combo
         * */
        boolean comboUpdateFlag = comboDao.update(combo);

        if(!comboUpdateFlag){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ServiceResponse.getMsg("Internal server error while updating Combo"));
        }

        return ResponseEntity.status(HttpStatus.OK).body(combo);



    }

    @RequestMapping(value = "delete/{comboDetailsId}",method = RequestMethod.GET)
    public ResponseEntity<?> delComboProduct(@Valid editDistributorForm editDistributorForm,
                                            BindingResult result,
                                            @PathVariable Integer comboDetailsId){

        ServiceResponse serviceResponse=ServiceResponse.getInstance();

        ComboDetails comboDetails=comboDetailDao.getById(comboDetailsId);

       if(comboDetails == null) {
            serviceResponse.setValidationError("ComboProductId", "No combo product found");
           return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(serviceResponse);
       }

       SellsDetails sellsDetails=sellDetailsDao.getByComboId(comboDetails.getComboId());

        if(sellsDetails!=null){
            serviceResponse.setValidationError("ComboProductId", "You are not eligible to delete this product.");
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(serviceResponse);
        }


        comboDetailDao.deleteComboProduct(comboDetails);


        return ResponseEntity.status(HttpStatus.OK).body(ServiceResponse.getMsg("Successfully deleted"));

    }


    @RequestMapping(value = "/active-inactive/{distributorId}/{activationType}",method = RequestMethod.POST)
    public ResponseEntity<?> editStatus(@Valid editDistributorForm editDistributorForm,
                                        BindingResult result,
                                        @PathVariable Integer distributorId,
                                        @PathVariable String activationType){
        int status;
        if(activationType.equals("activate")){
            status = 1;
        }else  if(activationType.equals("deactivate")){
            status = 0;
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ServiceResponse.getMsg("Uri segment wrong"));
        }
        Combo combo=comboDao.getById(distributorId);

        if(combo == null) {
            ServiceResponse serviceResponse = ServiceResponse.getInstance();
            serviceResponse.setValidationError("ComboId", "No distributor found");

            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(serviceResponse);

        }

        combo.setStatus(status);

        comboDao.update(combo);


        return ResponseEntity.status(HttpStatus.OK).body(combo);


    }

}
