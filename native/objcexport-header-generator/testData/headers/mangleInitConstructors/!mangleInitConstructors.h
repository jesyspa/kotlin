#import <Foundation/NSArray.h>
#import <Foundation/NSDictionary.h>
#import <Foundation/NSError.h>
#import <Foundation/NSObject.h>
#import <Foundation/NSSet.h>
#import <Foundation/NSString.h>
#import <Foundation/NSValue.h>

NS_ASSUME_NONNULL_BEGIN
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunknown-warning-option"
#pragma clang diagnostic ignored "-Wincompatible-property-type"
#pragma clang diagnostic ignored "-Wnullability"

#pragma push_macro("_Nullable_result")
#if !__has_feature(nullability_nullable_result)
#undef _Nullable_result
#define _Nullable_result _Nullable
#endif

__attribute__((objc_subclassing_restricted))
@interface Foo : Base
- (instancetype)initWithParam:(BOOL)param __attribute__((swift_name("init(param:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithParam_:(int32_t)param __attribute__((swift_name("init(param_:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithParam__:(NSString *)param __attribute__((swift_name("init(param__:)"))) __attribute__((objc_designated_initializer));
@property (readonly) NSString *param __attribute__((swift_name("param")));
@end

#pragma pop_macro("_Nullable_result")
#pragma clang diagnostic pop
NS_ASSUME_NONNULL_END
