package com.jackstraw.floatwindow.permission

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.provider.Settings
import android.util.Log

import java.lang.reflect.Method

/**
 * @author pink-jackstraw
 * @date 2018/11/19
 * @describe
 */
class FloatUtils {

    private val TAG = "FloatUtils"
    private var dialog: AlertDialog? = null

    fun checkPermission(mContext: Context): Boolean {
        //6.0 版本之后由于 google 增加了对悬浮窗权限的管理，所以方式就统一了
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                return miuiPermissionCheck(mContext)
            } else if (RomUtils.checkIsMeizuRom()) {
                return meizuPermissionCheck(mContext)
            } else if (RomUtils.checkIsHuaweiRom()) {
                return huaweiPermissionCheck(mContext)
            } else if (RomUtils.checkIs360Rom()) {
                return qikuPermissionCheck(mContext)
            } else if (RomUtils.checkIsOppoRom()) {
                return oppoROMPermissionCheck(mContext)
            }
        }
        return commonROMPermissionCheck(mContext)
    }

    fun applyPermission(mContext: Context) {
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                miuiROMPermissionApply(mContext)
            } else if (RomUtils.checkIsMeizuRom()) {
                meizuROMPermissionApply(mContext)
            } else if (RomUtils.checkIsHuaweiRom()) {
                huaweiROMPermissionApply(mContext)
            } else if (RomUtils.checkIs360Rom()) {
                ROM360PermissionApply(mContext)
            } else if (RomUtils.checkIsOppoRom()) {
                oppoROMPermissionApply(mContext)
            }
        } else {
            commonROMPermissionApply(mContext)
        }
    }

    private fun huaweiPermissionCheck(context: Context): Boolean {
        return HuaweiUtils.checkFloatWindowPermission(context)
    }

    private fun miuiPermissionCheck(context: Context): Boolean {
        return MiuiUtils.checkFloatWindowPermission(context)
    }

    private fun meizuPermissionCheck(context: Context): Boolean {
        return MeizuUtils.checkFloatWindowPermission(context)
    }

    private fun qikuPermissionCheck(context: Context): Boolean {
        return QikuUtils.checkFloatWindowPermission(context)
    }

    private fun oppoROMPermissionCheck(context: Context): Boolean {
        return OppoUtils.checkFloatWindowPermission(context)
    }

    private fun commonROMPermissionCheck(context: Context): Boolean {
        //最新发现魅族6.0的系统这种方式不好用，天杀的，只有你是奇葩，没办法，单独适配一下
        if (RomUtils.checkIsMeizuRom()) {
            return meizuPermissionCheck(context)
        } else {
            var result: Boolean? = true
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    val clazz = Settings::class.java
                    val canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context::class.java)
                    result = canDrawOverlays.invoke(null, context) as Boolean
                } catch (e: Exception) {
                    Log.e(TAG, Log.getStackTraceString(e))
                }

            }
            return result!!
        }
    }

    private fun ROM360PermissionApply(context: Context) {
        showConfirmDialog(context, object : OnConfirmResult {
            override fun confirmResult(confirm: Boolean) {
                if (confirm) {
                    QikuUtils.applyPermission(context)
                } else {
                    Log.e(TAG, "ROM:360, user manually refuse OVERLAY_PERMISSION")
                }
            }
        })
    }

    private fun huaweiROMPermissionApply(context: Context) {
        showConfirmDialog(context, object : OnConfirmResult {
            override fun confirmResult(confirm: Boolean) {
                if (confirm) {
                    HuaweiUtils.applyPermission(context)
                } else {
                    Log.e(TAG, "ROM:huawei, user manually refuse OVERLAY_PERMISSION")
                }
            }
        })
    }

    private fun meizuROMPermissionApply(context: Context) {
        showConfirmDialog(context, object : OnConfirmResult {
            override fun confirmResult(confirm: Boolean) {
                if (confirm) {
                    MeizuUtils.applyPermission(context)
                } else {
                    Log.e(TAG, "ROM:meizu, user manually refuse OVERLAY_PERMISSION")
                }
            }
        })
    }

    private fun miuiROMPermissionApply(context: Context) {
        showConfirmDialog(context, object : OnConfirmResult {
            override fun confirmResult(confirm: Boolean) {
                if (confirm) {
                    MiuiUtils.applyMiuiPermission(context)
                } else {
                    Log.e(TAG, "ROM:miui, user manually refuse OVERLAY_PERMISSION")
                }
            }
        })
    }

    private fun oppoROMPermissionApply(context: Context) {
        showConfirmDialog(context, object : OnConfirmResult {
            override fun confirmResult(confirm: Boolean) {
                if (confirm) {
                    OppoUtils.applyOppoPermission(context)
                } else {
                    Log.e(TAG, "ROM:miui, user manually refuse OVERLAY_PERMISSION")
                }
            }
        })
    }

    /**
     * 通用 rom 权限申请
     */
    private fun commonROMPermissionApply(context: Context) {
        //这里也一样，魅族系统需要单独适配
        if (RomUtils.checkIsMeizuRom()) {
            meizuROMPermissionApply(context)
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                showConfirmDialog(context, object : OnConfirmResult {
                    override fun confirmResult(confirm: Boolean) {
                        if (confirm) {
                            try {
                                MeizuUtils.commonROMPermissionApplyInternal(context)
                            } catch (e: Exception) {
                                Log.e(TAG, Log.getStackTraceString(e))
                            }

                        } else {
                            Log.d(TAG, "user manually refuse OVERLAY_PERMISSION")
                        }
                    }
                })
            }
        }
    }

    private fun showConfirmDialog(context: Context, result: OnConfirmResult) {
        showConfirmDialog(context, "您的手机没有授予悬浮窗权限，请开启后再试", result)
    }

    private fun showConfirmDialog(context: Context, message: String, result: OnConfirmResult) {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
        dialog = AlertDialog.Builder(context).setCancelable(true).setTitle("")
                .setMessage(message)
                .setPositiveButton("现在去开启") { dialog, _ ->
                    result.confirmResult(true)
                    dialog.dismiss()
                }.setNegativeButton("暂不开启") { dialog, _ ->
                    result.confirmResult(false)
                    dialog.dismiss()
                }.create()
        dialog!!.show()
    }

    interface OnConfirmResult {
        fun confirmResult(confirm: Boolean)
    }
}
