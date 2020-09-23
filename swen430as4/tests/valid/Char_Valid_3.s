
	.text
wl_rep:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $0, %rax
	movq %rax, -8(%rbp)
	movq 24(%rbp), %rax
	movq %rax, 16(%rbp)
	jmp label585
label585:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	subq $16, %rsp
	movq %rax, 0(%rsp)
	subq $32, %rsp
	movq -8(%rbp), %rbx
	movq %rbx, 8(%rsp)
	movq $119, %rbx
	movq %rbx, 16(%rsp)
	movq $101, %rbx
	movq %rbx, 24(%rsp)
	call wl_rep
	addq $32, %rsp
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq -48(%rsp), %rbx
	jmp label587
	movq $1, %rax
	jmp label588
label587:
	movq $0, %rax
label588:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	movq %rax, 0(%rsp)
	subq $32, %rsp
	movq -8(%rbp), %rbx
	movq %rbx, 8(%rsp)
	movq $122, %rbx
	movq %rbx, 16(%rsp)
	movq $72, %rbx
	movq %rbx, 24(%rsp)
	call wl_rep
	addq $32, %rsp
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq -48(%rsp), %rbx
	jmp label589
	movq $1, %rax
	jmp label590
label589:
	movq $0, %rax
label590:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	movq %rax, 0(%rsp)
	subq $32, %rsp
	movq -8(%rbp), %rbx
	movq %rbx, 8(%rsp)
	movq $49, %rbx
	movq %rbx, 16(%rsp)
	movq $111, %rbx
	movq %rbx, 24(%rsp)
	call wl_rep
	addq $32, %rsp
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq -48(%rsp), %rbx
	jmp label591
	movq $1, %rax
	jmp label592
label591:
	movq $0, %rax
label592:
	movq %rax, %rdi
	call assertion
label586:
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
