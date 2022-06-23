package io.iotex.pebble.pages.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.SimpleItemAnimator
import com.drakeet.multitype.MultiTypeAdapter
import io.iotex.core.base.BaseActivity
import io.iotex.pebble.R
import io.iotex.pebble.constant.PebbleStore
import io.iotex.pebble.module.viewmodel.ActivateVM
import io.iotex.pebble.module.viewmodel.PebbleVM
import io.iotex.pebble.module.walletconnect.WalletConnector
import io.iotex.pebble.pages.binder.NftEntry
import io.iotex.pebble.pages.binder.NftItemBinder
import io.iotex.pebble.pages.fragment.LoadingFragment
import io.iotex.pebble.utils.AddressUtil
import io.iotex.pebble.utils.extension.ellipsis
import io.iotex.pebble.utils.extension.gone
import io.iotex.pebble.utils.extension.updateItem
import io.iotex.pebble.utils.extension.visible
import io.iotex.pebble.widget.DisconnectDialog
import kotlinx.android.synthetic.main.activity_nft_list.*
import org.jetbrains.anko.startActivity

class NftListActivity : BaseActivity(R.layout.activity_nft_list) {

    private val mPebbleVM by lazy {
        ViewModelProvider(this, mVmFactory)[PebbleVM::class.java]
    }
    private val mActivateVM by lazy {
        ViewModelProvider(this, mVmFactory)[ActivateVM::class.java]
    }

    private val mAdapter = MultiTypeAdapter()

    private var mSelectedNft: NftEntry? = null

    private val mDevice by lazy {
        PebbleStore.mDevice
    }

    override fun initView(savedInstanceState: Bundle?) {
        mTvAddress.text = AddressUtil.getIoWalletAddress().ellipsis(6, 6)
        val binder = NftItemBinder().apply {
            setOnSelectedListener {
                mSelectedNft = it
                mTvApprove.isEnabled = true
                mTvActivate.isEnabled = true
                if (AddressUtil.isValidAddress(it.nft.approved ?: "")) {
                    mTvApprove.gone()
                    mTvActivate.visible()
                } else {
                    mTvApprove.visible()
                    mTvActivate.gone()
                }
            }
            setOnItemClickListener { nftEntry ->
                startActivity<NftDetailActivity>(
                    NftDetailActivity.KEY_TOKEN_ID to nftEntry.nft.tokenId,
                    NftDetailActivity.KEY_CONTRACT to nftEntry.contract,
                    NftDetailActivity.KEY_WALLET_ADDRESS to WalletConnector.walletAddress,
                )
            }
        }
        mAdapter.register(NftEntry::class, binder)
        mRvNft.adapter = mAdapter
        (mRvNft.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        mTvDisconnect.setOnClickListener {
            disconnectWallet()
        }
        mTvActivate.setOnClickListener {
            activateAndRegister()
        }
        mTvApprove.setOnClickListener {
            approve()
        }

        mTvSwitchWallet.setOnClickListener {
            WalletConnector.disconnect()
            WalletConnector.connect()
        }
        mTvWhereToBug.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("iopay://io.iotex.iopay/open?action=web&url=https://metapebble.app/faucet")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            applicationContext.startActivity(intent)
        }
    }

    private fun disconnectWallet() {
        DisconnectDialog(this)
            .setTitle(getString(R.string.disconnect_wallet))
            .setContent(getString(R.string.disconnect_wallet_warning))
            .setPositiveButton(getString(R.string.disconnect)) {
                WalletConnector.disconnect()
                this.onBackPressed()
            }.show()
    }

    private fun approve() {
        if (mDevice != null && mSelectedNft != null) {
            mActivateVM.approveRegistration(mSelectedNft?.nft?.tokenId ?: "")
        }
    }

    private fun activateAndRegister() {
        if (mDevice != null && mSelectedNft != null) {
            mActivateVM.signDevice(mDevice!!)
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        val address = WalletConnector.walletAddress
        if (!address.isNullOrBlank()) {
            mPebbleVM.queryNftList(AddressUtil.convertIoAddress(address))
        }
    }

    private fun activateProgress() {
        val step02 = LoadingFragment()
            .renderTitle(
                getString(R.string.registering_metapebble),
                getString(R.string.meta_pebble)
            )
            .setCompleteCallback {
                startActivity<ActivateCompleteActivity>()
                finish()
            }

        LoadingFragment()
            .renderTitle(getString(R.string.activating_metapebble), getString(R.string.meta_pebble))
            .setCompleteCallback {
                step02.start(supportFragmentManager, R.id.mRlContainer)
                    .complete()
            }
            .start(supportFragmentManager, R.id.mRlContainer)
            .complete()
    }

    override fun registerObserver() {
        mPebbleVM.mNftListLD.observe(this) {
            if (!it.isNullOrEmpty()) {
                mTlContentContainer.visible()
                mRlEmptyContainer.gone()
                mAdapter.items = it
                mAdapter.notifyDataSetChanged()
            } else {
                mTlContentContainer.gone()
                mRlEmptyContainer.visible()
            }
        }
        mActivateVM.mApproveLd.observe(this) { tokenId ->
            if (!tokenId.isNullOrBlank()) {
                val item = mAdapter.items.firstOrNull { item ->
                    if (item is NftEntry) {
                        item.nft.tokenId == tokenId
                    } else {
                        false
                    }
                } as? NftEntry
                mAdapter.updateItem(item) { nft ->
                    nft?.nft?.tokenId == item?.nft?.tokenId
                }
            }
            if (mSelectedNft?.nft?.tokenId == tokenId) {
                mTvApprove.gone()
                mTvActivate.visible()
            }
        }
        mActivateVM.mSignDeviceLD.observe(this) {
            if (it != null) {
                mDevice?.let { device ->
                    val tokenId = mSelectedNft!!.nft.tokenId.toString()
                    mActivateVM.activateMetaPebble(
                        tokenId,
                        device.pubKey,
                        it.imei,
                        it.sn,
                        it.timestamp.toString(),
                        it.authentication
                    )
                }
            }
        }
        mActivateVM.mActivateLd.observe(this) {
            activateProgress()
        }
    }
}