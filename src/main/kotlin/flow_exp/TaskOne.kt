// egen assignment
private val accountsAndBalance = mutableMapOf<String, Int>();

private val aNBEverythingCounted = mutableMapOf<String, Int>();

var transferCounter = 1

data class TransferRecord(
    val source: String,
    val des: String,
    val amount: Int,
    val expiration: Int,
)

private val transferRecordMap = mutableMapOf<String, TransferRecord>()

data class AccountNBalance(
    val account: String,
    val amount: Int,
)

fun solution(queries: MutableList<MutableList<String>>): MutableList<String> {
    val results = mutableListOf<String>()
    queries.forEach{ query->
        val operation = query[0]
        //val isDeposit = query.size == 4
        val account = query[2]
        when(operation){
            "CREATE_ACCOUNT"->{
                if(accountsAndBalance.contains(account)){
                    results.add("false")
                }else{
                    accountsAndBalance[account] = 0
                    aNBEverythingCounted[account] = 0
                    results.add("true")
                }
            }
            "DEPOSIT"->{
                val amount = query[3].toInt()
                if(accountsAndBalance.contains(account)){
                    accountsAndBalance[account] = accountsAndBalance[account]!! + amount
                    aNBEverythingCounted[account] = aNBEverythingCounted[account]!! + amount
                    results.add(accountsAndBalance[account].toString())
                }else{
                    results.add("")
                }
            }
            "PAY"->{
                val amount = query[3].toInt()
                if(accountsAndBalance.contains(account)){
                    if(accountsAndBalance[account]!! < amount){
                        results.add("")
                    }else{
                        accountsAndBalance[account] = accountsAndBalance[account]!! - amount
                        aNBEverythingCounted[account] = aNBEverythingCounted[account]!! + amount
                        results.add(accountsAndBalance[account].toString())
                    }
                }else{
                    results.add("")
                }
            }
            "TOP_ACTIVITY"->{
                var limit = query[2].toInt() - 1
                val allEntries = mutableListOf<AccountNBalance>()
                aNBEverythingCounted.forEach{ entry->
                    allEntries.add(AccountNBalance(entry.key, entry.value))
                }
                allEntries.sortWith(
                    compareByDescending<AccountNBalance>{
                        it.amount
                    }.thenBy{
                        it.account
                    }
                )
                //allEntries.reverse()
                limit = Math.min(limit, allEntries.size - 1)
                val onlyTopLimit = allEntries.slice(0..limit)
                var answer = ""
                onlyTopLimit.forEach{ entry->
                    answer += "${entry.account}(${entry.amount}), "
                }
                answer = answer.removeSuffix(", ")
                results.add(answer)
            }
            "TRANSFER"->{
                val timeStamp = query[1].toInt()
                val expiration = timeStamp + 86400000
                val source = query[2]
                val destination = query[3]
                val amount = query[4].toInt()
                // invalid source or destination account
                if(!accountsAndBalance.contains(source) || !accountsAndBalance.contains (destination)){
                    results.add("")
                }else if(accountsAndBalance[source]!! < amount){
                    // insufficient funds
                    results.add("")
                }else if(source == destination){
                    results.add("")
                }else{
                    // make entry
                    transferRecordMap["transfer$transferCounter"] = TransferRecord(
                        source,
                        destination,
                        amount,
                        expiration
                    )
                    results.add("transfer$transferCounter")
                    transferCounter++
                }
            }
            "ACCEPT_TRANSFER"->{
                // handle updating in and out flow when accepted
                val timeStamp = query[1].toInt()
                val destination = query[2]
                val transferId = query[3]
                if(!transferRecordMap.contains(transferId)){
                    results.add("false")
                    return@forEach
                }
                val transferObject = transferRecordMap[transferId]!!
                if(transferObject.expiration < timeStamp){
                    results.add("false")
                    return@forEach
                }
                if(transferObject.des != destination){
                    results.add("false")
                    return@forEach
                }
                aNBEverythingCounted[transferObject.source] = aNBEverythingCounted[transferObject.source]!! + transferObject.amount
                aNBEverythingCounted[transferObject.des] = aNBEverythingCounted[transferObject.des]!! + transferObject.amount
                transferRecordMap.remove(transferId)
                results.add("true")
            }

            else ->{

            }
        }
    }
    return results
}
