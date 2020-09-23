
	.text
wl_trim:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $0, %rax
	movq %rax, -16(%rbp)
	movq -8(%rbp), %rax
	movq %rax, 16(%rbp)
	jmp label753
label753:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	subq $32, %rsp
	movq $4, %rax
	movq %rax, 8(%rsp)
	call wl_trim
	addq $32, %rsp
	movq -32(%rsp), %rax
	movq %rax, -8(%rbp)
	movq -8(%rbp), %rax
	jmp label755
	movq $1, %rax
	jmp label756
label755:
	movq $0, %rax
label756:
	movq %rax, %rdi
	call assertion
label754:
	movq %rbp, %rsp
	popq %rbp
	ret
	.globl main
main:
	pushq %rbp
	call wl_main
	popq %rbp
	movq $0, %rax
	ret

	.data
