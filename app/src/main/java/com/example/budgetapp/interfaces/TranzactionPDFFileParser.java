package com.example.budgetapp.interfaces;

import com.example.budgetapp.model.TransactionModel;


import java.io.File;
import java.util.List;

public interface TranzactionPDFFileParser {

    List<TransactionModel> parseFile(File file);

}
