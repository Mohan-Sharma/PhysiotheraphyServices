package com.nzion.repository.billing.impl;

import java.math.BigDecimal;
import java.util.*;

import com.nzion.domain.billing.*;
import com.nzion.domain.emr.lab.LabTest;
import com.nzion.domain.emr.lab.LabTestProfile;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.nzion.domain.Employee;
import com.nzion.domain.Enumeration;
import com.nzion.domain.Patient;
import com.nzion.domain.Person;
import com.nzion.domain.Provider;
import com.nzion.domain.Referral;
/*import com.nzion.domain.Schedule;
import com.nzion.domain.SlotType;
import com.nzion.domain.SoapNoteType;
*/import com.nzion.domain.Speciality;
import com.nzion.domain.billing.Contract.CONTRACTTYPE;
//import com.nzion.domain.emr.Cpt;
import com.nzion.domain.emr.lab.LabOrderRequest;
import com.nzion.domain.emr.lab.LabTestPanel;
//import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.domain.pms.InsuranceProvider;
import com.nzion.report.search.view.BillingSearchVO;
import com.nzion.repository.billing.BillingRepository;
import com.nzion.repository.impl.HibernateBaseRepository;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilValidator;

import javax.rmi.CORBA.Util;

@SuppressWarnings("unchecked")
public class HibernateBillingRepository extends HibernateBaseRepository implements BillingRepository {

//	@Override
//	public List<CptPrice> getCptPricesFor(Collection<Cpt> cpts) {
//	Criteria criteria = getSession().createCriteria(CptPrice.class);
//	if (UtilValidator.isNotEmpty(cpts)) criteria.add(Restrictions.in("cpt", cpts));
//	return criteria.list();
//	}
//	
//	@Override
//	public List<CptPrice> getContractCptPricesFor(Contract contract, Collection<Cpt> cpts) {
//	Criteria criteria = getSession().createCriteria(CptPrice.class);	
//	if (UtilValidator.isNotEmpty(cpts)) criteria.add(Restrictions.in("cpt", cpts));
//	if(contract != null)
//		criteria.add(Restrictions.eq("contract", contract));
//	criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
//	return criteria.list();
//	}
	
	/*@Override
	public List<Invoice> getBillingTransactionFor(PatientSoapNote soapNote) {
	Criteria criteria = getSession().createCriteria(Invoice.class);
	criteria.add(Restrictions.eq("itemId", String.valueOf(soapNote.getId())));
	criteria.add(Restrictions.eq("itemType",PatientSoapNote.class.getName()));
	return criteria.list();
	}*/

	@Override
	public List<InvoiceItem> getTransactionItemsFor(Invoice billingTransaction) {
	Criteria criteria = getSession().createCriteria(InvoiceItem.class);
	criteria.add(Restrictions.eq("txn", billingTransaction));
	return criteria.list();
	}

	@Override
	public List<Consultation> getConsultationBySpeciality(Speciality speciality) {
	Criteria criteria = getSession().createCriteria(Consultation.class);
	criteria.add(Restrictions.eq("speciality", speciality));
	return criteria.list();
	}

	@Override
	public List<Consultation> getConsultationByProvider(Person provider) {
	Criteria criteria = getSession().createCriteria(Consultation.class);
	criteria.add(Restrictions.eq("person", provider));
	return criteria.list();
	}

	/*@Override
	public List<Consultation> getConsultationChargeFor(Speciality speciality,SoapNoteType soapNoteType) {
	Criteria criteria = getSession().createCriteria(Consultation.class);
	criteria.add(Restrictions.eq("speciality", speciality));
	if(soapNoteType!=null)
		criteria.add(Restrictions.eq("soapNoteType", soapNoteType));
	return criteria.list();
	}
*/
	/*@Override
	public List<Consultation> getConsultationChargeFor(Employee employee,SoapNoteType soapNoteType) {
	Criteria criteria = getSession().createCriteria(Consultation.class);
	criteria.add(Restrictions.eq("person", employee));
	if(soapNoteType!=null)
		criteria.add(Restrictions.eq("soapNoteType", soapNoteType));
	return criteria.list();
	}*/

	@Override
	public List<Invoice> getInvoice(List<InvoiceStatusItem> status,Patient patient,Employee emp,Date fromDate,Date thruDate,String ipNumber) {
		Criteria criteria = getSession().createCriteria(Invoice.class);
		criteria.add(Restrictions.isNotNull("patient"));
		if (UtilValidator.isNotEmpty(status))
			criteria.add(Restrictions.in("invoiceStatus", status));
		if(patient!=null)
			criteria.add(Restrictions.eq("patient", patient));
		if(emp!=null)
			criteria.add(Restrictions.eq("consultant", emp));
		if(UtilValidator.isNotEmpty(ipNumber))
			criteria.add(Restrictions.eq("ipNumber", ipNumber));
		if(fromDate != null) criteria.add(Restrictions.ge("invoiceDate",UtilDateTime.getDayStart(fromDate)));
		if(thruDate != null) criteria.add(Restrictions.le("invoiceDate",UtilDateTime.getDayEnd(thruDate)));
		criteria.addOrder(Order.desc("invoiceDate"));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		//criteria.addOrder(Order.desc("createdTxTimestamp"));
		if(patient==null && emp==null && UtilValidator.isEmpty(ipNumber) && fromDate == null && thruDate == null)
				criteria.setMaxResults(25);
		return criteria.list();
	}
	@Override
	public List<LabOrderRequest> getSearchByLabOrder(List<LabOrderRequest.ORDERSTATUS> status, Patient patient, Referral referral,Date fromDate,Date thruDate,String refDocName) {
		Criteria criteria = getSession().createCriteria(LabOrderRequest.class);
		if (UtilValidator.isNotEmpty(status))
			criteria.add(Restrictions.in("orderStatus", status));
		if(patient!=null)
			criteria.add(Restrictions.eq("patient", patient));
		/*if(provider!=null)
			criteria.add(Restrictions.eq("provider", provider));*/
		if(referral!=null)
			criteria.add(Restrictions.eq("referral", referral));
		if(fromDate != null)
			criteria.add(Restrictions.or(Restrictions.ge("startDate", fromDate),Restrictions.ge("phlebotomistStartDate", fromDate)));
			//criteria.add(Restrictions.ge("startDate",fromDate));
		if(thruDate != null)
			criteria.add(Restrictions.or(Restrictions.le("startDate", thruDate), Restrictions.le("phlebotomistStartDate", thruDate)));
			//criteria.add(Restrictions.le("startDate",thruDate));
		if((refDocName!=null) && (refDocName != ""))
			criteria.add(Restrictions.like("referralDoctorName",refDocName,MatchMode.ANYWHERE));
		criteria.addOrder(Order.desc("createdTxTimestamp"));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return criteria.list();
	}

	@Override
	public ArrayList<InvoicePayment> getInvoicePaymentsByCriteria(Patient patient, Date fromDate, Date thruDate) {
		Criteria criteria = getSession().createCriteria(InvoicePayment.class);
		if(fromDate != null)
			criteria.add(Restrictions.ge("paymentDate",UtilDateTime.getDayStart(fromDate)));
		if(thruDate != null)
			criteria.add(Restrictions.le("paymentDate", UtilDateTime.getDayEnd(thruDate)));

		ArrayList<InvoicePayment> list = new ArrayList<InvoicePayment>();
		if(UtilValidator.isNotEmpty(criteria.list())){
			Set<InvoicePayment> set = new  HashSet<InvoicePayment>(criteria.list());
			list.addAll(set);
		}
		return list;

	}

	@Override
	public Contract findEffectiveContractFor(String name, CONTRACTTYPE type, Date fromDate, Date thruDate) {	
	return null;
	}
	
	@Override
	public List<Contract> getContractForInsPro(InsuranceProvider insuPro) {
	Criteria criteria = getSession().createCriteria(Contract.class);
	if(insuPro != null){		
		criteria.createCriteria("insuranceProviders").add(Restrictions.eq("id", insuPro.getId()));		
	}
	return criteria.list();
	}

	@Override
	public List<InsuranceProvider> getInsuranceProviderAttachedToContract() {
	Criteria criteria = getSession().createCriteria(InsuranceProvider.class);
	criteria.add(Restrictions.isNotNull("contract"));
	return criteria.list();
	}

	@Override
	public List<Invoice> searchInvoiceBy(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate) {
	Criteria criteria = getSession().createCriteria(Invoice.class);
	if(billingSearchVO.getPatient() != null)
		criteria.add(Restrictions.eq("patient",billingSearchVO.getPatient()));
	
	
	/*if(billingSearchVO.getPaymentMethod() != null){
		Criteria invPaymentCriteria = criteria.createCriteria("invoicePayments");
		invPaymentCriteria.add(Restrictions.eq("paymentMethod", billingSearchVO.getPaymentMethod()));
	}*/
	
	if(UtilValidator.isNotEmpty(billingSearchVO.getIpNumber()))
		criteria.add(Restrictions.like("ipNumber", billingSearchVO.getIpNumber(), MatchMode.START));
	
	if(billingSearchVO.getCollectedByUser() != null)
		criteria.add(Restrictions.eq("collectedByUser",billingSearchVO.getCollectedByUser()));
	
	if(billingSearchVO.getConsultant() != null)
		criteria.add(Restrictions.eq("consultant",billingSearchVO.getConsultant()));
	if(fromDate != null) criteria.add(Restrictions.ge("invoiceDate",UtilDateTime.getDayStart(fromDate)));
	if(thruDate != null) criteria.add(Restrictions.le("invoiceDate",UtilDateTime.getDayEnd(thruDate)));
	if(UtilValidator.isNotEmpty(billingSearchVO.getStatus()) || UtilValidator.isNotEmpty(billingSearchVO.getOrStatus())){
		criteria.add(Restrictions.or(Restrictions.eq("invoiceStatus", billingSearchVO.getStatus()), Restrictions.eq("invoiceStatus", billingSearchVO.getOrStatus())));
	}else if(UtilValidator.isNotEmpty(billingSearchVO.getStatus()))
		criteria.add(Restrictions.eq("invoiceStatus", billingSearchVO.getStatus()));
	
	if ("EQUAL".equalsIgnoreCase(billingSearchVO.getLowEndAmtQuantifier()))
		criteria.add(Restrictions.eq("totalAmount.amount", billingSearchVO.getLowAmntRange().getAmount()));
	else
		if ("LESS".equalsIgnoreCase(billingSearchVO.getLowEndAmtQuantifier()))
			criteria.add(Restrictions.lt("totalAmount.amount", billingSearchVO.getLowAmntRange().getAmount()));
		else
			if ("Greater".equalsIgnoreCase(billingSearchVO.getLowEndAmtQuantifier()))
				criteria.add(Restrictions.gt("totalAmount.amount", billingSearchVO.getLowAmntRange().getAmount()));
			else
				if ("Between".equalsIgnoreCase(billingSearchVO.getLowEndAmtQuantifier())) {
					criteria.add(Restrictions.gt("totalAmount.amount",billingSearchVO.getLowAmntRange().getAmount()));
					criteria.add(Restrictions.lt("totalAmount.amount",billingSearchVO.getHighAmntRange().getAmount()));
				}
	/*if(billingSearchVO.getCpt() != null){
		criteria.createCriteria("invoiceItems").add(Restrictions.eq("itemId", billingSearchVO.getCpt().getId()))
		.add(Restrictions.eq("itemType", InvoiceType.OPD_PROCEDURE));
	}*/
	criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	return criteria.list();
	}

	@Override
	public List<Invoice> searchReferralInvoiceBy(BillingSearchVO billingSearchVO, Date fromDate, Date thruDate) {
		Criteria criteria = getSession().createCriteria(Invoice.class);
		criteria.add(Restrictions.gt("totalReferralAmountTobePaid", new BigDecimal(0.0)));
		if (billingSearchVO.getConsultant() != null)
			criteria.add(Restrictions.eq("consultant", billingSearchVO.getConsultant()));
		if (fromDate != null)
			criteria.add(Restrictions.ge("invoiceDate", UtilDateTime.getDayStart(fromDate)));
		if (thruDate != null)
			criteria.add(Restrictions.le("invoiceDate", UtilDateTime.getDayEnd(thruDate)));
		if (billingSearchVO.getSelectedReferralDoctor() != null)
			criteria.add(Restrictions.eq("referralDoctorFirstName", billingSearchVO.getSelectedReferralDoctor().getFirstName()));
		if (billingSearchVO.getSelectedReferralDoctor() != null)
			criteria.add(Restrictions.eq("referralDoctorLastName", billingSearchVO.getSelectedReferralDoctor().getLastName()));
	//	criteria.createAlias("referralContract","referralContractAlias");
		if(billingSearchVO.getReferral() != null)
			//criteria.add(Restrictions.eq("referralContractAlias.referral",billingSearchVO.getReferral()));
			criteria.add(Restrictions.eq("referralConsultantId", billingSearchVO.getReferral().getId()));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return criteria.list();
	}

	@Override
    public BigDecimal[] getInvoiceTotal(Invoice invoice) {
        Criteria criteria = getSession().createCriteria(InvoiceItem.class, "item");
        criteria.add(Restrictions.eq("invoice",invoice));
        criteria.setProjection(Projections.projectionList().add(Projections.sum("price.amount")));
        BigDecimal totalAmt= (BigDecimal)criteria.uniqueResult();

        criteria = getSession().createCriteria(InvoicePayment.class, "item");
        criteria.add(Restrictions.eq("invoice",invoice));
        criteria.setProjection(Projections.projectionList().add(Projections.sum("amount.amount")));
        BigDecimal totalAdvanceAmt= (BigDecimal)criteria.uniqueResult();

        return new BigDecimal[]{totalAmt,totalAdvanceAmt};
    }
    
    @Override
	public List<Invoice> getFirstInvoice(Patient patient) {
	Criteria criteria = getSession().createCriteria(Invoice.class);
	criteria.add(Restrictions.eq("patient", patient));
	criteria.addOrder(Order.asc("id"));
	return  criteria.list();
	}

    
   

	@Override
	public LabTestPanel getLabtestPanelByPanelName(String panelName) {
		Criteria criteria = getSession().createCriteria(LabTestPanel.class);
		criteria.add(Restrictions.eq("panelName", panelName));
		return (LabTestPanel) criteria.uniqueResult();
	}
	
	@Override
	public List<AcctgTransaction> searchAcctgTransactionBy(AcctgTransTypeEnum chargeType,Date fromDate,Date thruDate) {
		Criteria criteria = getSession().createCriteria(AcctgTransaction.class);
		if(UtilValidator.isNotEmpty(chargeType))
			criteria.add(Restrictions.eq("acctgTransTypeEnum",chargeType));
		if(fromDate != null)
			criteria.add(Restrictions.ge("transactionDate",UtilDateTime.getDayStart(fromDate)));
		if(thruDate != null)
			criteria.add(Restrictions.le("transactionDate",UtilDateTime.getDayEnd(thruDate)));
		return criteria.list();
		}

	public List<AcctgTransactionEntry> searchAcctgTransactionEntryForLabReport(Date fromDate,Date thruDate,Object chargeType){
		
		Criteria criteria = getSession().createCriteria(AcctgTransactionEntry.class);
		if(fromDate != null)
			criteria.add(Restrictions.ge("transactionDate",UtilDateTime.getDayStart(fromDate)));
		if(thruDate != null)
			criteria.add(Restrictions.le("transactionDate",UtilDateTime.getDayEnd(thruDate)));
		else
			criteria.add(Restrictions.eq("transactionType",AcctgTransTypeEnum.OPD_LAB_CHARGES));
		
		return criteria.list();
	}
	
public List<AcctgTransactionEntry> searchAcctgTransactionEntryForLabReportExport(Date fromDate,Date thruDate,Object chargeType,Object provider, Object referral){
		
		Criteria criteria = getSession().createCriteria(AcctgTransactionEntry.class);
		if(fromDate != null)
			criteria.add(Restrictions.ge("transactionDate",UtilDateTime.getDayStart(fromDate)));
		if(thruDate != null)
			criteria.add(Restrictions.le("transactionDate",UtilDateTime.getDayEnd(thruDate)));
		else
			criteria.add(Restrictions.eq("transactionType",AcctgTransTypeEnum.OPD_LAB_CHARGES));
		
		
		if(provider!=null && provider instanceof String)
			criteria.add(Restrictions.isNotNull("doctorId"));
		if(provider!=null && provider instanceof Provider)
			criteria.add(Restrictions.eq("doctorId", ((Provider)provider).getId().toString()));
		
		if(referral!=null && referral instanceof String)
			criteria.add(Restrictions.isNotNull("referralId"));
		if(referral!=null && referral instanceof Referral)
			criteria.add(Restrictions.eq("referralId", ((Referral)referral).getId().toString()));
		return criteria.list();
	}

	public  List<LabTest> getPriceForLabTest(List<LabTest> labTests){
		Iterator iterator = labTests.iterator();
		while (iterator.hasNext()){
			LabTest labTest = (LabTest)iterator.next();
			String testCode = labTest.getTestCode();

			String query = "SELECT BILLABLE_AMOUNT, home_service FROM lab_tariff where LAB_TEST = '" + testCode + "'";
			Query q = getSession().createSQLQuery(query);
			List l = q.list();
			if(UtilValidator.isNotEmpty(l)){
				Object[] data = (Object[]) l.get(0);
				labTest.setBillableAmount((BigDecimal) data[0]);
				labTest.setHomeServiceAmount((BigDecimal) data[1]);
			}
		}
		return labTests;
	}

	public  boolean updatePriceInLabTariff(List<LabTest> labTests){
		try {
			Iterator iterator = labTests.iterator();
			while (iterator.hasNext()){
				LabTest labTest = (LabTest)iterator.next();
				String query = "UPDATE lab_tariff SET BILLABLE_AMOUNT = " + labTest.getBillableAmount() + ", home_service = " + labTest.getHomeServiceAmount() + " WHERE LAB_TEST = '" + labTest.getTestCode() + "'";
				Query q = getSession().createSQLQuery(query);
				int count = q.executeUpdate();
				if((count == 0) && ((labTest.getBillableAmount() != null) || (labTest.getHomeServiceAmount() != null))){
					String insertQuery = "INSERT INTO lab_tariff (SERVICE_MAIN_GROUP,SERVICE_SUB_GROUP,INS_SERVICE_ID,TARIF_CATEGORY,PATIENT_CATEGORY,LABORATORY_SHARE,DOCTOR_SHARE,TECHNICIAN_SHARE,TEST_COST,MARKUP_AMOUNT,BILLABLE_AMOUNT,home_service,FROM_DATE,THRU_DATE,LOCATION_ID,LAB_TEST) VALUES ('07','009',262,'01','01',125.00,100.00,30.00,255.00,50.00,"+labTest.getBillableAmount()+","+labTest.getHomeServiceAmount()+",'"+UtilDateTime.nowDateString("yyyy-MM-dd")+"','2018-12-31',10001,'"+labTest.getTestCode()+"')";
					Query iq = getSession().createSQLQuery(insertQuery);
					int insertCount = iq.executeUpdate();
				} else if ((count == 1) && (labTest.getHomeServiceAmount() == null)){
					String insertQuery = "DELETE FROM lab_tariff WHERE LAB_TEST="+labTest.getTestCode();
					Query iq = getSession().createSQLQuery(insertQuery);
					int insertCount = iq.executeUpdate();
				}
			}
		} catch (Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public  List<LabTestPanel> getPriceForLabTestPanel(List<LabTestPanel> labTestPanels){
		Iterator iterator = labTestPanels.iterator();
		Session session = getSession();
		try {
			while (iterator.hasNext()) {
				LabTestPanel labTest = (LabTestPanel) iterator.next();
				String testCode = labTest.getPanelCode();

				String query = "SELECT BILLABLE_AMOUNT, home_service FROM lab_tariff where LAB_PANEL = '" + testCode + "'";
				Query q = session.createSQLQuery(query);
				List l = q.list();
				if (UtilValidator.isNotEmpty(l)) {
					Object[] data = (Object[]) l.get(0);
					labTest.setBillableAmount((BigDecimal) data[0]);
					labTest.setHomeServiceAmount((BigDecimal) data[1]);
				}
			}
			return labTestPanels;
		} catch (Exception e){
			e.printStackTrace();
			return Collections.EMPTY_LIST;
		} finally {
			if (session != null){
				releaseSession(session);
			}
		}
	}

	public  boolean updatePriceInLabTariffForLabTestPanel(List<LabTestPanel> labTestPanels){
		try {
			Iterator iterator = labTestPanels.iterator();
			while (iterator.hasNext()){
				LabTestPanel labTest = (LabTestPanel)iterator.next();
				String query = "UPDATE lab_tariff SET BILLABLE_AMOUNT = " + labTest.getBillableAmount() + ", home_service = " + labTest.getHomeServiceAmount() + " WHERE LAB_PANEL = '" + labTest.getPanelCode() + "'";
				Query q = getSession().createSQLQuery(query);
				int count = q.executeUpdate();
				if((count == 0) && ((labTest.getBillableAmount() != null) || (labTest.getHomeServiceAmount() != null))){
					String insertQuery = "INSERT INTO lab_tariff (SERVICE_MAIN_GROUP,SERVICE_SUB_GROUP,INS_SERVICE_ID,TARIF_CATEGORY,PATIENT_CATEGORY,LABORATORY_SHARE,DOCTOR_SHARE,TECHNICIAN_SHARE,TEST_COST,MARKUP_AMOUNT,BILLABLE_AMOUNT,home_service,FROM_DATE,THRU_DATE,LOCATION_ID,LAB_PANEL) VALUES ('07','009',262,'01','01',125.00,100.00,30.00,255.00,50.00,"+labTest.getBillableAmount()+","+labTest.getHomeServiceAmount()+",'"+UtilDateTime.nowDateString("yyyy-MM-dd")+"','2018-12-31',10001,'"+labTest.getPanelCode()+"')";
					Query iq = getSession().createSQLQuery(insertQuery);
					int insertCount = iq.executeUpdate();
				} else if ((count == 1) && (labTest.getHomeServiceAmount() == null)){
					String insertQuery = "DELETE FROM lab_tariff WHERE LAB_PANEL="+labTest.getPanelCode();
					Query iq = getSession().createSQLQuery(insertQuery);
					int insertCount = iq.executeUpdate();
				}
			}
		} catch (Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public  List<LabTestProfile> getPriceForLabTestProfile(List<LabTestProfile> labTestProfiles){
		Iterator iterator = labTestProfiles.iterator();
		while (iterator.hasNext()){
			LabTestProfile labTest = (LabTestProfile)iterator.next();
			String testCode = labTest.getProfileCode();

			String query = "SELECT BILLABLE_AMOUNT, home_service FROM lab_tariff where LAB_PROFILE = '" + testCode + "'";
			Query q = getSession().createSQLQuery(query);
			List l = q.list();
			if(UtilValidator.isNotEmpty(l)){
				Object[] data = (Object[]) l.get(0);
				labTest.setBillableAmount((BigDecimal) data[0]);
				labTest.setHomeServiceAmount((BigDecimal) data[1]);
			}
		}
		return labTestProfiles;
	}

	public  boolean updatePriceInLabTariffForLabTestProfile(List<LabTestProfile> labTestProfiles){
		try {
			Iterator iterator = labTestProfiles.iterator();
			while (iterator.hasNext()){
				LabTestProfile labTest = (LabTestProfile)iterator.next();
				String query = "UPDATE lab_tariff SET BILLABLE_AMOUNT = " + labTest.getBillableAmount() + ", home_service = " + labTest.getHomeServiceAmount() + " WHERE LAB_PROFILE = '" + labTest.getProfileCode() + "'";
				Query q = getSession().createSQLQuery(query);
				int count = q.executeUpdate();
				if((count == 0) && ((labTest.getBillableAmount() != null) || (labTest.getHomeServiceAmount() != null))){
					String insertQuery = "INSERT INTO lab_tariff (SERVICE_MAIN_GROUP,SERVICE_SUB_GROUP,INS_SERVICE_ID,TARIF_CATEGORY,PATIENT_CATEGORY,LABORATORY_SHARE,DOCTOR_SHARE,TECHNICIAN_SHARE,TEST_COST,MARKUP_AMOUNT,BILLABLE_AMOUNT,home_service,FROM_DATE,THRU_DATE,LOCATION_ID,LAB_PROFILE) VALUES ('07','009',262,'01','01',125.00,100.00,30.00,255.00,50.00,"+labTest.getBillableAmount()+","+labTest.getHomeServiceAmount()+",'"+UtilDateTime.nowDateString("yyyy-MM-dd")+"','2018-12-31',10001,'"+labTest.getProfileCode()+"')";
					Query iq = getSession().createSQLQuery(insertQuery);
					int insertCount = iq.executeUpdate();
				}
			}
		} catch (Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
}
