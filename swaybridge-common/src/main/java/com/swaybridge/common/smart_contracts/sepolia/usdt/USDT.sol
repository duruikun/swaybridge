// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

/**
 * @title Sepolia测试网USDT合约（复刻主网核心功能）
 * 对齐主网USDT：6位小数、ERC-20核心方法、转账返回bool
 */
contract Usdt {
    // ===== 核心属性（和主网USDT一致）=====
    string public constant name = "Tether USD";
    string public constant symbol = "USDT";
    uint8 public constant decimals = 6; // 主网USDT小数位6
    uint256 public totalSupply; // 总供应量

    // ===== 核心映射（ERC-20标准）=====
    mapping(address => uint256) public balanceOf; // 地址余额
    mapping(address => mapping(address => uint256)) public allowance; // 授权额度
    address public owner;

    // ===== 事件（和主网USDT一致）=====
    event Transfer(address indexed from, address indexed to, uint256 value);
    event Approval(address indexed owner, address indexed spender, uint256 value);

    // ===== 权限控制（简化版）=====
    modifier onlyOwner() {
        require(msg.sender == owner, "TestUSDT: only owner");
        _;
    }

    constructor() {
        owner = msg.sender;
    }

    function transfer(address to, uint256 value) external returns (bool) {
        require(to != address(0), "TestUSDT: transfer to zero address");
        require(balanceOf[msg.sender] >= value, "TestUSDT: insufficient balance");

        // 扣减发送方余额，增加接收方余额
        balanceOf[msg.sender] -= value;
        balanceOf[to] += value;

        emit Transfer(msg.sender, to, value);
        return true;
    }

    function approve(address spender, uint256 value) external returns (bool) {
        require(spender != address(0), "TestUSDT: approve to zero address");

        allowance[msg.sender][spender] = value;
        emit Approval(msg.sender, spender, value);
        return true;
    }

    function transferFrom(address from, address to, uint256 value) external returns (bool) {
        require(to != address(0), "TestUSDT: transfer to zero address");
        require(balanceOf[from] >= value, "TestUSDT: insufficient balance");
        require(allowance[from][msg.sender] >= value, "TestUSDT: allowance exceeded");

        // 扣减授权额度、付款方余额，增加收款方余额
        allowance[from][msg.sender] -= value;
        balanceOf[from] -= value;
        balanceOf[to] += value;

        emit Transfer(from, to, value);
        return true;
    }

    function mint(address to, uint256 value) external onlyOwner {
        require(to != address(0), "TestUSDT: mint to zero address");

        totalSupply += value;
        balanceOf[to] += value;

        emit Transfer(address(0), to, value);
    }

    /**
     * @dev 暂停/恢复转账（模拟主网USDT的风控功能， optional）
     */
    bool public paused;
    event Paused(bool status);

    function setPaused(bool _paused) external onlyOwner {
        paused = _paused;
        emit Paused(_paused);
    }

}