package com.swaybridge.common.utils;

import com.swaybridge.common.enums.StandardEventSignatureEnum;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.Arrays;

public class EventFactory {

    public static Event stdErc20TransferEvent() {
        return new Event(
                StandardEventSignatureEnum.ERC20_ERC721_TRANSFER_EVENT.getEventName(),
                Arrays.asList(
                        new TypeReference<Address>(true) {},
                        new TypeReference<Address>(true) {},
                        new TypeReference<Uint256>(false) {}
                )
        );
    }

    public static Event stdErc721TransferEvent() {
        return new Event(
                StandardEventSignatureEnum.ERC20_ERC721_TRANSFER_EVENT.getEventName(),
                Arrays.asList(
                        new TypeReference<Address>(true) {},
                        new TypeReference<Address>(true) {},
                        new TypeReference<Uint256>(true) {}
                )
        );
    }

}
