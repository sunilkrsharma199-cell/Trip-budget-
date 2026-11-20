package com.example.domain

import com.example.data.local.ExpenseEntity
import com.example.data.local.TravelerEntity
import kotlin.math.abs

data class TravelerBalance(
    val name: String,
    val totalPaidUsd: Double,
    val fairShareUsd: Double,
    val netBalanceUsd: Double // Positive = owed money, Negative = owes money
)

data class SettlementInstruction(
    val from: String,
    val to: String,
    val amountUsd: Double
)

data class SplitSummary(
    val balances: List<TravelerBalance>,
    val settlements: List<SettlementInstruction>
)

object ExpenseSplitter {

    fun calculate(
        travelers: List<TravelerEntity>,
        expenses: List<ExpenseEntity>
    ): SplitSummary {
        val names = travelers.map { it.name.trim() }.filter { it.isNotEmpty() }.distinct()
        if (names.isEmpty()) return SplitSummary(emptyList(), emptyList())

        val paidMap = names.associateWith { 0.0 }.toMutableMap()
        val shareMap = names.associateWith { 0.0 }.toMutableMap()

        for (expense in expenses) {
            if (expense.amountUsd <= 0.0) continue

            val payer = if (paidMap.containsKey(expense.paidByName)) {
                expense.paidByName
            } else {
                names.first()
            }
            paidMap[payer] = (paidMap[payer] ?: 0.0) + expense.amountUsd

            val participants = expense.splitAmongCsv
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() && shareMap.containsKey(it) }
                .ifEmpty { names }

            val perPersonShare = expense.amountUsd / participants.size
            for (person in participants) {
                shareMap[person] = (shareMap[person] ?: 0.0) + perPersonShare
            }
        }

        val balances = names.map { name ->
            val paid = paidMap[name] ?: 0.0
            val share = shareMap[name] ?: 0.0
            TravelerBalance(
                name = name,
                totalPaidUsd = paid,
                fairShareUsd = share,
                netBalanceUsd = paid - share
            )
        }

        // Greedy min-cash-flow settlement
        val creditors = balances
            .filter { it.netBalanceUsd > 0.01 }
            .map { it.name to it.netBalanceUsd }
            .sortedByDescending { it.second }
            .toMutableList()

        val debtors = balances
            .filter { it.netBalanceUsd < -0.01 }
            .map { it.name to abs(it.netBalanceUsd) }
            .sortedByDescending { it.second }
            .toMutableList()

        val settlements = mutableListOf<SettlementInstruction>()
        var cIdx = 0
        var dIdx = 0

        while (cIdx < creditors.size && dIdx < debtors.size) {
            val (creditorName, creditAmt) = creditors[cIdx]
            val (debtorName, debtAmt) = debtors[dIdx]

            val transfer = minOf(creditAmt, debtAmt)
            if (transfer > 0.01) {
                settlements += SettlementInstruction(
                    from = debtorName,
                    to = creditorName,
                    amountUsd = transfer
                )
            }

            val remCredit = creditAmt - transfer
            val remDebt = debtAmt - transfer

            if (remCredit <= 0.01) cIdx++ else creditors[cIdx] = creditorName to remCredit
            if (remDebt <= 0.01) dIdx++ else debtors[dIdx] = debtorName to remDebt
        }

        return SplitSummary(balances = balances, settlements = settlements)
    }
}
