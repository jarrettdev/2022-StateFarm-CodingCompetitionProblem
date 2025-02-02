package com.statefarm.codingcompetition.simpledatatool.controller;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.statefarm.codingcompetition.simpledatatool.model.Agent;
import com.statefarm.codingcompetition.simpledatatool.model.Claim;
import com.statefarm.codingcompetition.simpledatatool.model.Customer;
import com.statefarm.codingcompetition.simpledatatool.model.Policy;

public class SimpleDataToolController {

    /**
     * Read in a CSV file and return a list of entries in that file
     * 
     * @param <T>
     * @param filePath  Path to the file being read in
     * @param classType Class of entries beng read in
     * @return List of entries from CSV file
     */
    public <T> List<T> readCsvFile(String filePath, Class<T> classType) {
        List<T> entries = new ArrayList<>();
        try
        {
            CsvSchema bootstrapSchema = CsvSchema.emptySchema().withHeader();
            CsvMapper mapper = new CsvMapper();
            mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
            ObjectReader oReader = mapper.readerFor(classType).with(bootstrapSchema);
            Reader reader = new FileReader(filePath);
            MappingIterator<T> mi = oReader.readValues(reader);
            while (mi.hasNext())
            {
                entries.add(mi.next());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return entries;
    }

    /**
     * Gets the number of open claims
     * 
     * @param claims List of all claims
     * @return number of open claims
     */
    public int getNumberOfOpenClaims(List<Claim> claims) {
        return (int) claims.stream()
                           .filter(claim -> claim.getIsClaimOpen() == true)
                           .count();
    }

    /**
     * Get the number of customer for an agent id
     * 
     * @param filePath File path to the customers CSV
     * @param agentId  Agent id as int
     * @return number of customer for agent
     */
    public int getNumberOfCustomersForAgentId(String filePath, int agentId) {
        List<Customer> customers = readCsvFile(filePath, Customer.class);
        return (int) customers.stream()
                              .filter(customer -> customer.getAgentId() == agentId)
                              .count();
    }

    /**
     * Get the number of customer for an agent id
     * 
     * @param filePath File path to the customers CSV
     * @param state    Agent id as int
     * @return number of customer for agent
     */
    public int getNumberOfAgentsForState(String filePath, String state) {
        List<Agent> agents = readCsvFile(filePath, Agent.class);
        return (int) agents.stream()
                           .filter(agent -> agent.getState().equals(state))
                           .count();
    }

    /**
     * Sum total premium for a specific customer id
     * 
     * @param policies   List of all policies
     * @param customerId Customer id as int
     * @return float of monthly premium
     */
    public double sumMonthlyPremiumForCustomerId(List<Policy> policies, int customerId) {
        return policies.stream()
                       .filter(policy -> policy.getCustomerId() == customerId)
                       .mapToDouble(policy -> policy.getPremiumPerMonth())
                       .sum();
    }

    /**
     * For a given customer (by first and last names), return the number of open
     * claims they have
     * 
     * @param filePathToCustomer File path to customers CSV
     * @param filePathToPolicy   File path to policies CSV
     * @param filePathToClaims   File path to claims CSV
     * @param firstName          First name of customer to search for
     * @param lastName           Last name of customer to search for
     * @return Number of open claims for customer or null if customer doesn't exist
     */
    public Integer getNumberOfOpenClaimsForCustomerName(String filePathToCustomer, String filePathToPolicy,
            String filePathToClaims, String firstName, String lastName) {

        /*Read Customer, Policy, Claims csv files*/
        List<Customer>  customers   = readCsvFile(filePathToCustomer, Customer.class);
        List<Policy>    policies    = readCsvFile(filePathToPolicy, Policy.class);
        List<Claim>     claims      = readCsvFile(filePathToClaims, Claim.class);

        getNumberOfOpenClaims(claims);

        Optional<Customer> customer = customers
                .stream()
                .filter(c -> c.getFirstName().equals(firstName) && c.getLastName().equals(lastName))
                .findFirst();

        if (customer.isPresent()) {
            List<Policy> customerPolicies   = policies
                    .stream()
                    .filter(policy -> policy.getCustomerId() == customer.get().getId())
                    .collect(Collectors.toList());
            List<Claim> customerClaims      = claims
                    .stream()
                    .filter(claim -> customerPolicies.stream().anyMatch(policy -> policy.getId() == claim.getPolicyId()))
                    .collect(Collectors.toList());

            return getNumberOfOpenClaims(customerClaims);
        }
        return null;
    }

    /**
     * Returns the most spoken language (besides English) for customers in a given
     * state
     * 
     * @param customersFilePath File path to customers CSV
     * @param state             State abbreviation ex: AZ, TX, IL, etc.
     * @return String of language
     */
    public String getMostSpokenLanguageForState(String customersFilePath, String state) {

        List<Customer> customers = readCsvFile(customersFilePath, Customer.class);
        Map<String,Integer> language = new HashMap<>();

        List<Customer> customers_in_state = customers
                .stream()
                .filter(customer -> customer.getState().equals(state))
                .collect(Collectors.toList());



        for (Customer customer : customers_in_state)
        {
            if (!customer.getPrimaryLanguage().equals("English"))
            {
                if (language.containsKey(customer.getPrimaryLanguage()))
                {
                    language.put
                        (
                        customer.getPrimaryLanguage(),
                        language.get(customer.getPrimaryLanguage())+1
                        );
                }
                else
                {
                    language.put
                        (
                        customer.getPrimaryLanguage(),
                        1
                        );
                }
            }

            if (!customer.getSecondaryLanguage().equals(""))
            {
                if (!customer.getSecondaryLanguage().equals("English"))
                {
                    if (language.containsKey(customer.getSecondaryLanguage()))
                    {
                        language.put
                            (
                            customer.getSecondaryLanguage(),
                            language.get(customer.getSecondaryLanguage())+1
                            );
                    }
                    else
                    {
                        language.put
                            (
                            customer.getSecondaryLanguage(),
                            1
                            );
                    }
                }
            }
        }
        Map.Entry<String, Integer> maxEntry = null;
        for (Map.Entry<String, Integer> entry : language.entrySet())
        {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
            {
                maxEntry = entry;
            }
        }
        return maxEntry.getKey();
    }

    /**
     * Returns Customer with the highest, total premium
     * 
     * @param customersFilePath File path to customers CSV
     * @param policies          List of all policies
     * @return Customer that has the highest, total premium as Customer object
     */
    public Customer getCustomerWithHighestTotalPremium(String customersFilePath, List<Policy> policies) {
        List<Customer>         customers        = readCsvFile(customersFilePath, Customer.class);
        Map<Integer, Double>   customerPremiums = new HashMap<>();
        for (Customer customer : customers) {
            if (customerPremiums.containsKey(customer.getId()))
            {
                customerPremiums.put
                    (
                    customer.getId(),
                    customerPremiums.get(customer.getId()) + sumMonthlyPremiumForCustomerId(policies, customer.getId())
                    );
            }
            else
            {
                customerPremiums.put
                    (
                    customer.getId(),
                    sumMonthlyPremiumForCustomerId(policies, customer.getId())
                    );
            }
        }
        Map.Entry<Integer, Double> maxEntry = null;
        for (Map.Entry<Integer, Double> entry : customerPremiums.entrySet())
        {
            if (maxEntry == null ||
                entry.getValue() > (maxEntry.getValue()))
            {
                maxEntry = entry;
            }

        }
        for (Customer customer : customers)
        {
            if (customer.getId() == maxEntry.getKey())
            {
                return customer;
            }
        }
        return null;
    }


    /**
     * Returns the total number of open claims for a given state
     * 
     * @param customersFilePath File path to customers CSV
     * @param policiesFilePath  File path to policies CSV
     * @param claimsFilePath    File path to claims CSV
     * @param state             State abbreviation ex: AZ, TX, IL, etc.
     * @return number of open claims as int
     */
    public int getOpenClaimsForState(String customersFilePath, String policiesFilePath, String claimsFilePath,
            String state) {

        List <Claim>    claims      = readCsvFile(claimsFilePath, Claim.class);
        List <Customer> customers   = readCsvFile(customersFilePath, Customer.class);
        List <Policy>   policies    = readCsvFile(policiesFilePath, Policy.class);

        List <Claim>    open_claims                 = claims
                .stream()
                .filter(claim -> claim.getIsClaimOpen())
                .collect(Collectors.toList());

        List <Policy>   open_claims_policies        = policies
                .stream()
                .filter(policy -> open_claims
                        .stream()
                        .anyMatch(claim -> claim.getPolicyId() == policy.getId()))
                .collect(Collectors.toList());

        List <Customer> open_claims_customers       = customers
                .stream()
                .filter(customer -> open_claims_policies
                        .stream()
                        .anyMatch(policy -> policy.getCustomerId() == customer.getId()))
                .collect(Collectors.toList());

        int open_claims_state = (int) open_claims_customers
                .stream()
                .filter(customer -> customer.getState().equals(state))
                .count();

        return open_claims_state;
    }

    /**
     * Builds a dictionary/map of agents and their total premium from their
     * customers
     * 
     * @param customersFilePath File path to customers CSV
     * @param policiesFilePath  File path to policies CSV
     * @return Map of agent id as int to agent's total premium as double
     */
    public Map<Integer, Double> buildMapOfAgentPremiums(
            String customersFilePath, String policiesFilePath) {

        List<Customer>      customers   = readCsvFile(customersFilePath, Customer.class);
        List<Policy>        policies    = readCsvFile(policiesFilePath, Policy.class);

        Map<Integer,Double> agent       = new HashMap<>();
        for (Customer customer : customers)
        {
            if (agent.containsKey(customer.getAgentId()))
            {
                agent.put
                    (
                    customer.getAgentId(),
                    agent.get(customer.getAgentId())+sumMonthlyPremiumForCustomerId(policies,customer.getId())
                    );
            } else {
                agent.put
                    (
                    customer.getAgentId(),
                    sumMonthlyPremiumForCustomerId(policies,customer.getId())
                    );
            }
        }
        return agent;
    }
}